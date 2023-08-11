package com.akkaapp;

import akka.actor.typed.ActorSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.Random;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.DoubleSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.JSONException;
import org.json.JSONObject;

public class App {

    public static void main(String[] args) {
        ActorSystem<Void> mySystem = ActorSystem.create(FinancialApp.create(), "mySystem");
//        System.out.println(mySystem.printTree());

//        try { Thread.sleep(10000); } catch (InterruptedException e) { System.out.println(e); }

//        System.out.println(mySystem.printTree());

//        System.out.println("test");
//        mySystem.terminate();
    }
}

