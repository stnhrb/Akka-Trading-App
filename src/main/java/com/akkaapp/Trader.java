package com.akkaapp;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.util.*;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class Trader extends AbstractBehavior<Trader.Signal> {

    private KafkaConsumer<String, Double> consumer;
    interface Signal { }

    public static class Shares implements Signal {
        public final ArrayList<HashMap<String, Object>> traderShares;

        public Shares(ArrayList<HashMap<String, Object>> traderShares) {
            this.traderShares = traderShares;
        }
    }

    public static class BuySignal implements Signal {
        public final String companyName;
        public final ActorRef<Broker.Request> brokerRef;

        public BuySignal(String companyName, ActorRef<Broker.Request> brokerRef) {
            this.companyName = companyName;
            this.brokerRef = brokerRef;
        }
    }

    public static class SellSignal implements Signal {
        public final String companyName;
        public final int numOfShares;
        public final ActorRef<Broker.Request> brokerRef;

        public SellSignal(String companyName, int numOfShares, ActorRef<Broker.Request> brokerRef) {
            this.companyName = companyName;
            this.numOfShares = numOfShares;
            this.brokerRef = brokerRef;
        }
    }

    private double balance;
    private ArrayList<HashMap<String, Object>> shares;

    public static Behavior<Signal> create(double balance){
        return Behaviors.setup(context -> new Trader(context, balance));
    }

    private Trader(ActorContext<Signal> context, double balance) {
       super(context);
       this.balance = balance;
       this.shares = new ArrayList<>();
       this.consumer = KafkaHelper.prepareKafkaConsumer(getContext().getSelf());
    }

    private Behavior<Signal> buyShare(BuySignal buySignal) {
        ConsumerRecord<String, Double> latest_quote = KafkaHelper.quoteConsumer(buySignal.companyName, consumer);
        if (balance >= latest_quote.value()) {
            balance = balance - latest_quote.value();
            if (latest_quote != null) {
                System.out.println("the required quote is + " + latest_quote.key() + " and its value is " + latest_quote.value() + " the offset is " + latest_quote.offset());
                buySignal.brokerRef.tell(new Broker.BuyRequest(latest_quote.key(), latest_quote.value(), getContext().getSelf()));
            } else
                System.out.println("Consumer poll records did not have any new quotes for the required company since the last time it polled.");
            return this;
        } else {
            System.out.println("There is not enough balance to buy the required share: " + latest_quote.key() + " price: " + latest_quote.value() + ". Current balance: " + balance);
        }

        return this;
    }

    private Behavior<Signal> sellShare(SellSignal sellSignal) {
        // TODO:
        //  1- continue to implement the selling logic for trader.
        //  * here the shares are not updating after every buy request, instead they update after all the buy requests
        //  get acknowledged by the auditor which result in queueing the update shares messages to be at the end of the
        //  trader actor mailbox queue, which means any sell message queued after a buy message will not find the shares
        //  of that buy request.
        //  Mailbox queue example :[buy, buy, buy, sell, updateShares, updateShares updateShares].
        //  in the above example the sell request will not find any shares.

        if (!shares.isEmpty())
        {
            shares.iterator().forEachRemaining(share -> {
                if ( ((String) share.get("company")).equalsIgnoreCase(sellSignal.companyName)
                        && ((Integer) share.get("num_of_shares")) <= sellSignal.numOfShares)
                    System.out.println("share " + sellSignal.companyName + " sold");
            });
        }else {
            System.out.println("Trader@" + getContext().getSelf().path().uid() + " has no shares");
        }
        return this;
    }

    private Behavior<Signal> updateShares(Shares shares) {
        this.shares = shares.traderShares;
        System.out.println("trader shares updated | shares = " + this.shares);

        return this;
    }

    private Behavior<Signal> onPostStop() {
        consumer.close();
        return this;
    }

    @Override
    public Receive<Signal> createReceive() {
        return newReceiveBuilder()
                .onMessage(BuySignal.class, this::buyShare)
                .onMessage(SellSignal.class, this::sellShare)
                .onMessage(Shares.class, this::updateShares)
                .onSignal(PostStop.class, signal -> onPostStop()).build();
    }
}

















