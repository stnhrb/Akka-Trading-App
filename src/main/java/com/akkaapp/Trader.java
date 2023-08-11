package com.akkaapp;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.HashMap;

public class Trader extends AbstractBehavior<Trader.Request> {

    interface Request { }
    public static class BuyOrder implements Request {
        public final String compName;

        public BuyOrder(String compName) {
            this.compName = compName;
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
       KafkaConsumer<String, Double> consumer = this.prepareKafkaConsumer();
    }

    private KafkaConsumer<String, Double> prepareKafkaConsumer() {
        String bootstrapServers = "127.0.0.1:9092";
        String groupId = "my-app-1";

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, DoubleDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        consumer = new KafkaConsumer<>(properties);

        return consumer;
    }

    private Behavior<Request> QuoteConsumer(BuyOrder order) {
        System.out.println("inside the QuoteConsumer ...");

        String topicToConsumeFrom = "market";
        consumer.subscribe(Arrays.asList(topicToConsumeFrom));

        ConsumerRecords<String, Double> records = consumer.poll(Duration.ofSeconds(1));

        records.forEach(record -> {
            if (record.key().equals(order.compName))
                System.out.println("the required quote is + " + record.key() + " and its value is " + record.value() + " the offset is " + record.offset());
        });

        return this;
    }

    @Override
    public Receive<Request> createReceive() {
        return newReceiveBuilder()
                .onMessage(BuyOrder.class, this::QuoteConsumer)
                .onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private Behavior<Request> onPostStop() {
        consumer.close();
        return this;
    }
}