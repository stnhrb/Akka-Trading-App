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

public class Trader extends AbstractBehavior<Trader.Request> {

    interface Request { }
    public static class BuyRequest implements Request {
        public final String companyName;
        public final ActorRef<Auditor.Transaction> auditorRef;

        public BuyRequest(String companyName, ActorRef<Auditor.Transaction> auditorRef) {
            this.companyName = companyName;
            this.auditorRef = auditorRef;
        }
    }

    public static class SellRequest implements Request {
        public final String companyName;
        public final double sellPrice;
        public final ActorRef<Auditor.Transaction> auditorRef;

        public SellRequest(String companyName, double sellPrice, ActorRef<Auditor.Transaction> auditorRef) {
            this.companyName = companyName;
            this.sellPrice = sellPrice;
            this.auditorRef = auditorRef;
        }
    }

    private double balance;
    private HashMap<String, Double> shares;
    private KafkaConsumer<String, Double> consumer;

    public static Behavior<Request> create(){
        return Behaviors.setup(context -> new Trader(context));
    }

    private Trader(ActorContext<Request> context) {
       super(context);
       consumer = this.prepareKafkaConsumer();
    }

    private KafkaConsumer<String, Double> prepareKafkaConsumer() {
        String bootstrapServers = "localhost:9092";
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

        consumer.subscribe(Arrays.asList(topicToConsumeFrom));
        ConsumerRecords<String, Double> records  = consumer.poll(Duration.ofSeconds(10));

        Iterator recordsIterator = records.iterator();

        ConsumerRecord<String, Double> latest_quote = null;
        while(recordsIterator.hasNext()) {
            ConsumerRecord<String, Double> rcd = (ConsumerRecord<String, Double>) recordsIterator.next();
            if(rcd.key().equals(companyName))
                latest_quote = rcd;
        }

        return latest_quote;
    }

    private Behavior<Request> buyShare(BuyRequest buyRequest) {
        ConsumerRecord<String, Double> latest_quote = this.quoteConsumer(buyRequest.companyName);

        if (latest_quote != null)
            System.out.println("the required quote is + " + latest_quote.key() + " and its value is " + latest_quote.value() + " the offset is " + latest_quote.offset());
        else
            System.out.println("Consumer poll records did not have any new quotes for the required company since the last time it polled.");

        buyRequest.auditorRef.tell(new Auditor.BuyTransaction(latest_quote.key(), latest_quote.value(), getContext().getSelf()));

        return this;
    }

    private Behavior<Request> onPostStop() {
        consumer.close();
        return this;
    }

    @Override
    public Receive<Request> createReceive() {
        return newReceiveBuilder()
                .onMessage(BuyRequest.class, this::buyShare)
                .onSignal(PostStop.class, signal -> onPostStop()).build();
    }
}

















