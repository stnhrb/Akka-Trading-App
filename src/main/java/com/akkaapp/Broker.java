package com.akkaapp;

import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.japi.pf.ReceiveBuilder;

public class Broker extends AbstractBehavior<Broker.Request> {

    interface Request { }

    public static class BuyRequest implements Request {
        public final String companyName;
        public final double price;
        public final ActorRef<Trader.Signal> TraderRef;

        public BuyRequest(String companyName, double price, ActorRef<Trader.Signal> TraderRef) {
            this.companyName = companyName;
            this.price = price;
            this.TraderRef = TraderRef;
        }
    }

    public static class SellRequest implements Broker.Request {
        public final String companyName;
        public final double sellPrice;
        public final ActorRef<Auditor.Transaction> auditorRef;

        public SellRequest(String companyName, double sellPrice, ActorRef<Auditor.Transaction> auditorRef) {
            this.companyName = companyName;
            this.sellPrice = sellPrice;
            this.auditorRef = auditorRef;
        }
    }

    private ActorRef<Auditor.Transaction> auditor;

    public static Behavior<Request> create(ActorRef<Auditor.Transaction> auditor) {
        return Behaviors.setup(context -> new Broker(context, auditor));
    }

    private Broker(ActorContext<Request> context, ActorRef<Auditor.Transaction> auditor) {
        super(context);
        this.auditor = auditor;
    }

    @Override
    public Receive<Request> createReceive() {
        return newReceiveBuilder()
                .onMessage(BuyRequest.class, this::AcknowledgeBuyRequest)
                .build();
    }

    private Behavior<Request> AcknowledgeBuyRequest(BuyRequest buyRequest) {
        auditor.tell(new Auditor.BuyTransaction(buyRequest.companyName, buyRequest.price, buyRequest.TraderRef));
        return this;
    }

}
