package com.akkaapp;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.MailboxSelector;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.time.Duration;


public class FinancialApp extends AbstractBehavior<Void> {

    public static Behavior<Void> create() {
        return Behaviors.setup(context -> new FinancialApp(context));
    }

    private FinancialApp(ActorContext<Void> context) {
        super(context);

        ActorRef<QuoteGenerator.GenerateQuote> quoteGenerator
                = context.spawn(QuoteGenerator.create(), "QuoteGenerator");

        ActorRef<TimedQuoteMessenger.Command> timedQuoteMessenger
                =  context.spawn(TimedQuoteMessenger.create(quoteGenerator, Duration.ofSeconds(4)), "TimedQuoteMessenger");

        timedQuoteMessenger.tell(new TimedQuoteMessenger.Start());

        ActorRef<Trader.Signal> trader
                = context.spawn(Trader.create(250), "Trader", MailboxSelector.fromConfig("my-app.my-special-mailbox"));

        ActorRef<Auditor.Transaction> audit
                = context.spawn(Auditor.create(), "Auditor");

        ActorRef<Broker.Request> broker
                = context.spawn(Broker.create(audit), "Broker");

        trader.tell(new Trader.BuySignal("NVDA", broker));
        trader.tell(new Trader.BuySignal("MSFT", broker));
        trader.tell(new Trader.BuySignal("AAPL", broker));
        trader.tell(new Trader.BuySignal("AMZN", broker));
        trader.tell(new Trader.BuySignal("META", broker));
        trader.tell(new Trader.BuySignal("NVDA", broker));
        trader.tell(new Trader.BuySignal("NVDA", broker));

        trader.tell(new Trader.SellSignal("NVDA", 1, broker));
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private Behavior<Void> onPostStop() {
        System.out.println("FinancialApp stopped");
        return this;
    }
}