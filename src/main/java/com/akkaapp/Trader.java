package com.akkaapp;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.DoubleSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

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

    public static Behavior<Request> create(){
        return Behaviors.setup(context -> new Trader(context));
    }

    private Trader(ActorContext<Request> context) {
       super(context);
       KafkaConsumer();
    }

    private void KafkaConsumer() {
        System.out.println("inside kafka consumer");

        String bootstrapServers = "127.0.0.1:9092";
        String groupId = "my-app-1";
        String topic = "market";

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, DoubleDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        KafkaConsumer<String, Double> consumer = new KafkaConsumer<>(properties);

        consumer.subscribe(Arrays.asList(topic));

        while(true){
            // TODO: specify the company to be consumed and take the latest produced price
            ConsumerRecords<String, Double> records = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, Double> record : records){
                System.out.println("Key: " + record.key() + ", Value: " + record.value() + ", timestamp: " + record.timestamp());
                System.out.println("Partition: " + record.partition() + ", Offset:" + record.offset());
            }
        }
    }

    @Override
    public Receive<Request> createReceive() {
        return null;
    }
}