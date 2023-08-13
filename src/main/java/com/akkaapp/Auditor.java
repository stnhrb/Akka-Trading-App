package com.akkaapp;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Auditor extends AbstractBehavior<Auditor.Transaction> {

    public interface Transaction { }
    public static class BuyTransaction implements Transaction {
        public final String companyName;
        public final Double price;

        public BuyTransaction(String companyName, Double price) {
            this.companyName = companyName;
            this.price = price;
        }
    }

    public static Behavior<Transaction> create() {
        return Behaviors.setup(context -> new Auditor(context));
    }

    public Auditor(ActorContext<Transaction> context) {
        super(context);
    }

    private Behavior<Transaction> AcknowledgeBuyTransaction(BuyTransaction buyTransaction) {
        // TODO:
        //  validate the received buy or sell transaction
        //  store the transaction info in a db
        return this;
    }

    @Override
    public Receive<Transaction> createReceive() {
        return newReceiveBuilder()
                .onMessage(BuyTransaction.class, this::AcknowledgeBuyTransaction).build();
    }
}