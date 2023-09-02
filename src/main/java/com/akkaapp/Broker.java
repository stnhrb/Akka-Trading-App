package com.akkaapp;

import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;

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
        public final int numOfShares;
        public final ActorRef<Trader.Signal> TraderRef;

        public SellRequest(String companyName, int numOfShares, ActorRef<Trader.Signal> TraderRef) {
            this.companyName = companyName;
            this.numOfShares = numOfShares;
            this.TraderRef = TraderRef;
        }
    }

    private ActorRef<Auditor.Transaction> auditor;
    private KafkaConsumer<String, Double> consumer;


    public static Behavior<Request> create(ActorRef<Auditor.Transaction> auditor) {
        return Behaviors.setup(context -> new Broker(context, auditor));
    }

    private Broker(ActorContext<Request> context, ActorRef<Auditor.Transaction> auditor) {
        super(context);
        this.auditor = auditor;
        this.consumer = KafkaHelper.prepareKafkaConsumer(getContext().getSelf());
    }

    @Override
    public Receive<Request> createReceive() {
        return newReceiveBuilder()
                .onMessage(BuyRequest.class, this::AcknowledgeBuyRequest)
                .onMessage(SellRequest.class, this::AcknowledgeSellRequest)
                .build();
    }

    private Behavior<Request> AcknowledgeBuyRequest(BuyRequest buyRequest) {
        auditor.tell(new Auditor.BuyTransaction(buyRequest.companyName, buyRequest.price, buyRequest.TraderRef));
        return this;
    }

    private Behavior<Request> AcknowledgeSellRequest(SellRequest sellRequest) {
        // TODO: implement the broker selling logic
        boolean foundPriceMatch = matchSellingRequest(sellRequest);
        return this;
    }

    private boolean matchSellingRequest(SellRequest sellRequest) {
        // TODO: implement sell request matching logic
        for (int i = 0; i < sellRequest.numOfShares; i++)
        {
            ConsumerRecord<String, Double> latest_quote = KafkaHelper.quoteConsumer(sellRequest.companyName, consumer);
            System.out.println("The sell request of " + latest_quote.key() + "is acknowledged and the selling price of share " + i  + " is " + latest_quote.value());
        }

        return false;
    }


}
