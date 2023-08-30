package com.akkaapp;

import akka.actor.typed.ActorRef;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.DoubleSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class KafkaHelper {

    public static KafkaProducer<String, Double> prepareKafkaProducer() {
        String bootstrapServers = "localhost:29092";

        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, DoubleSerializer.class.getName());

        return new KafkaProducer<>(properties);
    }

    public static KafkaConsumer<String, Double> prepareKafkaConsumer(ActorRef<?> actor) {
        String bootstrapServers = "localhost:29092";
        String groupId = actor.path().name() + "@" + actor.path().uid();

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, DoubleDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        return new KafkaConsumer<>(properties);
    }

    public static ConsumerRecord<String, Double> quoteConsumer(String companyName, KafkaConsumer<String, Double> consumer) {
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

}
