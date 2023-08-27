package com.akkaapp;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


import java.time.Duration;
import java.util.*;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

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
        public final double sellPrice;
        public final ActorRef<Broker.Request> brokerRef;

        public SellSignal(String companyName, double sellPrice, ActorRef<Broker.Request> brokerRef) {
            this.companyName = companyName;
            this.sellPrice = sellPrice;
            this.brokerRef = brokerRef;
        }
    }

    private double balance;
    private ArrayList<HashMap<String, Object>> shares = null;

    public static Behavior<Signal> create(double balance){
        return Behaviors.setup(context -> new Trader(context, balance));
    }

    private Trader(ActorContext<Signal> context, double balance) {
       super(context);
       this.balance = balance;
       this.consumer = this.prepareKafkaConsumer();
    }

    private KafkaConsumer<String, Double> prepareKafkaConsumer() {
        String bootstrapServers = "localhost:29092";
        String groupId = "Trader@" + getContext().getSelf().path().uid();

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, DoubleDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        consumer = new KafkaConsumer<>(properties);
        return consumer;
    }

    private ConsumerRecord<String, Double> quoteConsumer(String companyName) {
        String topicToConsumeFrom = "market";

        consumer.subscribe(List.of(topicToConsumeFrom));
        ConsumerRecords<String, Double> records  = consumer.poll(Duration.ofSeconds(10));

        Iterator<ConsumerRecord<String, Double>> recordsIterator = records.iterator();

        // TODO: try to modify this null
        ConsumerRecord<String, Double> latest_quote = null;
        while(recordsIterator.hasNext()) {
            ConsumerRecord<String, Double> rcd = recordsIterator.next();
            if(rcd.key().equals(companyName))
                latest_quote = rcd;
        }

        return latest_quote;
    }

    private Behavior<Signal> buyShare(BuySignal buySignal) {
        ConsumerRecord<String, Double> latest_quote = this.quoteConsumer(buySignal.companyName);

        if (this.balance >= latest_quote.value()) {
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

















