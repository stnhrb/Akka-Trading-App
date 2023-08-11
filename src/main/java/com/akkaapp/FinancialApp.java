package com.akkaapp;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
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
                =  context.spawn(TimedQuoteMessenger.create(quoteGenerator, Duration.ofSeconds(3)), "TimedQuoteMessenger");

        timedQuoteMessenger.tell(new TimedQuoteMessenger.Start());

        ActorRef<Trader.Request> trader
                = context.spawn(Trader.create(), "Trader");

        trader.tell(new Trader.BuyOrder("NVDA"));
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