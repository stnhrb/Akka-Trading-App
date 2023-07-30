package com.akkaapp;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashMap;

public class Trader extends AbstractBehavior<Trader.Order> {

    interface Order { }
    public static class BuyOrder implements Order {
        public final String compName;

        public BuyOrder(String compName) {
            this.compName = compName;
        }
    }

    private double balance;
    private HashMap<String, Double> shares;

    public static Behavior<Order> create(){
        return Behaviors.setup(context -> new Trader(context));
    }

    private Trader(ActorContext<Order> context){
       super(context);
    }
    @Override
    public Receive<Order> createReceive() {
        return null;
    }
}








// Kafak consumer code
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.kafka.clients.consumer.ConsumerRecords;
//import org.apache.kafka.clients.consumer.KafkaConsumer;
//import org.apache.kafka.clients.producer.KafkaProducer;
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.apache.kafka.clients.producer.RecordMetadata;
//import org.apache.kafka.common.serialization.DoubleSerializer;
//import org.apache.kafka.common.serialization.StringDeserializer;
//import org.apache.kafka.common.serialization.StringSerializer;
//        String bootstrapServers = "127.0.0.1:9092";
//        String groupId = "my-app-1";
//        String topic = "test";
//
//        // create consumer configs
//        Properties properties = new Properties();
//        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
//        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
//        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
//        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//
//        // create consumer
//        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
//        // subscribe consumer to our topic(s)
//        consumer.subscribe(Arrays.asList(topic));
//
//        while(true){
//            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
//
//            for (ConsumerRecord<String, String> record : records){
//                System.out.println("Key: " + record.key() + ", Value: " + record.value() + ", timestamp: " + record.timestamp());
//                System.out.println("Partition: " + record.partition() + ", Offset:" + record.offset());
//            }
//        }