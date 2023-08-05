package com.akkaapp;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.DoubleSerializer;
import org.apache.kafka.common.serialization.StringSerializer;


public class QuoteGenerator extends AbstractBehavior<Void> {

    private enum Companies {
        MSFT,
        AAPL,
        AMZN,
        NVDA,
        META
    }

    public static Behavior<Void> create() {
        return Behaviors.setup(context -> new QuoteGenerator(context));
    }

    private QuoteGenerator(ActorContext<Void> context) {
        super(context);
        publishQuotesToKafkaStream();
    }

    private KafkaProducer<String, Double> producer;

    private HashMap<String, Double> generateQuotes() {
        HashMap<String, Double> quotes = new HashMap<>();

        for(Companies company: Companies.values())
            quotes.put(company.name(), 20 + (60 - 20) * (new Random().nextDouble()));

        return quotes;
    }

    private KafkaProducer<String, Double> prepareKafkaProducer() {
        String bootstrapServers = "127.0.0.1:9092";

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, DoubleSerializer.class.getName());

        producer = new KafkaProducer<>(properties);

        return producer;
    }


    private void publishQuotesToKafkaStream() {
        KafkaProducer<String, Double> producer = prepareKafkaProducer();

        while (true) {
            generateQuotes().forEach(
                    (company, price) -> {
                        ProducerRecord<String, Double> producerRecord =
                                new ProducerRecord<>("market", company, price);
                        producer.send(producerRecord);
                        producer.flush();

                        getContext().getLog().info("Quote sent: \n Company: " + producerRecord.key()+ " Price: " + producerRecord.value());
                        System.out.println("Quote sent: \n Company: " + producerRecord.key()+ " Price: " + producerRecord.value());

                    });

            try { Thread.sleep(10000); } catch (InterruptedException e) { System.out.println(e); }

            }
   }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder()
                .onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private Behavior<Void> onPostStop() {
        System.out.println("Closing Kafka stream ...");
        producer.close();
        System.out.println("Quote Generator stopped");
        return this;
    }
}