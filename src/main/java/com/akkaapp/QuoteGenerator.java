package com.akkaapp;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.*;

import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.DoubleSerializer;
import org.apache.kafka.common.serialization.StringSerializer;


class QuoteGenerator extends AbstractBehavior<QuoteGenerator.GenerateQuote> {
    public static final class GenerateQuote { }
    private enum Companies {
        MSFT,
        AAPL,
        AMZN,
        NVDA,
        META
    }

    public static Behavior<GenerateQuote> create() {
        return Behaviors.setup(context -> new QuoteGenerator(context));
    }

    private QuoteGenerator(ActorContext<GenerateQuote> context) {
        super(context);
    }

    private KafkaProducer<String, Double> producer;

    private HashMap<String, Double> generateQuotes() {
        HashMap<String, Double> quotes = new HashMap<>();

        for(Companies company: Companies.values())
            quotes.put(company.name(), 20 + (60 - 20) * (new Random().nextDouble()));

        return quotes;
    }

    private Behavior<GenerateQuote> publishQuotesToKafkaStream(GenerateQuote gq) {
        KafkaProducer<String, Double> producer = KafkaHelper.prepareKafkaProducer();

        generateQuotes().forEach(
                (company, price) -> {
                    ProducerRecord<String, Double> producerRecord =
                            new ProducerRecord<>("market", company, price);
                    producer.send(producerRecord);
//                    System.out.println("Quote sent: \n Company: " + producerRecord.key() + " Price: " + producerRecord.value());
                });
        producer.flush();
        return this;
    }

    @Override
    public Receive<GenerateQuote> createReceive() {
        return newReceiveBuilder()
                .onMessage(GenerateQuote.class, this::publishQuotesToKafkaStream)
                .onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private Behavior<GenerateQuote> onPostStop() {
        getContext().getLog().info("Closing Kafka stream ...");
        producer.close();
        getContext().getLog().info("Quote Generator stopped");
        return this;
    }
}
