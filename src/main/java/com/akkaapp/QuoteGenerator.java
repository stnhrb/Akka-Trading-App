package com.akkaapp;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Random;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
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

    private String companeyQoute() throws IOException {
        URL url = new URL("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=IBM&apikey=WX1VVKD4QIZQKBHX");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        int status = con.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        System.out.println(status);
        System.out.println(content);

        return "";
    }

    private void publishQuotesToKafkaStream() {
        String bootstrapServers = "127.0.0.1:9092";

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        while (true) {
            for(Companies company: Companies.values()){
                double companyPrice = 10 + (100 - 10) * (new Random().nextDouble());

                ProducerRecord<String, String> producerRecord =
                        new ProducerRecord<>("market", company.toString(), Double.toString(companyPrice));

                producer.send(producerRecord);
                System.out.println("Quote sent: \n Company: " + producerRecord.key()+ " Price: " + producerRecord.value());
            }
            producer.flush();
            try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }

        // flush and close producer
//        producer.close();
    }

    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder()
                .onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private Behavior<Void> onPostStop() {
        System.out.println("Quote Generator stopped");
        return this;
    }
}