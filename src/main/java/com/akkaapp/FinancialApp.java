package com.akkaapp;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


public class FinancialApp extends AbstractBehavior<Void> {
    private final ActorRef<Void> queryActorRef;

    public static Behavior<Void> create() {
        return Behaviors.setup(context -> new FinancialApp(context));
    }

    private FinancialApp(ActorContext<Void> context) {
        super(context);
        // TODO: spawn the rest of the app actors here
        queryActorRef = context.spawn(QuoteGenerator.create(), "QuoteGenerator");
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