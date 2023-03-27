package com.akkaapp;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.util.Random;

public class QuoteGenerator extends AbstractBehavior<QuoteGenerator.Query> {

    interface Query { }

    public static class Company implements Query {
        public final String companyName;

        public Company(String companyName) {
            this.companyName = companyName;
        }
    }

    public static Behavior<Query> create() {
        return Behaviors.setup(context -> new QuoteGenerator(context));
    }

    private QuoteGenerator(ActorContext<Query> context) {
        super(context);
    }
    private String company = "";

    @Override
    public Receive<Query> createReceive() {
        return newReceiveBuilder()
                .onMessage(Company.class, this::generateQuote)
                .build();
    }

    private Behavior<Query> generateQuote(Company company) {
        this.company = company.companyName;

        /* TODO: generate quote price */

        double quotePrice = this.generateQuotePrice();

        /* TODO: sends a message to kafka actor in order to be published in a kafka topic. */
        System.out.println("========================================");
        System.out.println("Company Name: " + company.companyName);
        System.out.println("Company Price: " + quotePrice);
        System.out.println("========================================");

        return this;
    }

    private double generateQuotePrice() {
        Random r = new Random();
        double rangeMin = 5;
        double rangeMax = 25;
        double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();

        return randomValue;
    }
}