package com.akkaapp;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class FinancialApp extends AbstractBehavior<FinancialApp.StartApp> {

    public static class StartApp {
        private final String appName;

        public StartApp(String appName) {
            this.appName = appName;
        }
    }

    private final ActorRef<QuoteGenerator.Query> queryActorRef;

    public static Behavior<StartApp> create() {
        return Behaviors.setup(context -> new FinancialApp(context));
    }

    private FinancialApp(ActorContext<StartApp> context) {
        super(context);
        queryActorRef = context.spawn(QuoteGenerator.create(), "QuoteGenerator");
    }

    @Override
    public Receive<StartApp> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartApp.class, this::onStart).build();
    }

    private Behavior<StartApp> onStart(StartApp start) {
        queryActorRef.tell(new QuoteGenerator.Company("ABC"));

        return this;
    }
}