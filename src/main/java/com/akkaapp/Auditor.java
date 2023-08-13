package com.akkaapp;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Auditor extends AbstractBehavior<Auditor.Transaction> {

    public interface Transaction { }
    public static class BuyTransaction implements Transaction {
        public final String companyName;
        public final Double price;

        public BuyTransaction(String companyName, Double price) {
            this.companyName = companyName;
            this.price = price;
        }
    }

    private Connection DatabaseConnection;

    public static Behavior<Transaction> create() {
        return Behaviors.setup(context -> new Auditor(context));
    }

    public Auditor(ActorContext<Transaction> context) {
        super(context);
        DatabaseConnection = this.prepareDatabaseConnection();
        createTransactionsTable(DatabaseConnection);
    }

    private Connection prepareDatabaseConnection() {
        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "test123");

        try {
            DatabaseConnection = DriverManager.getConnection(url, props);
        } catch (SQLException e) {
            System.out.println(e);
        }

        return DatabaseConnection;
    }

    private void createTransactionsTable(Connection DatabaseConnection) {
        try {
            Statement createStatement = DatabaseConnection.createStatement();
            String sqlStatement = "CREATE TABLE Transactions (" +
                                    "id serial PRIMARY KEY NOT NULL, " +
                                    "company VARCHAR ( 10 ) NOT NULL, " +
                                    "price NUMERIC(6,4) NOT NULL, " +
                                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL)";
            createStatement.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
    private String insertIntoDb() {
        return "";
    }

    private Behavior<Transaction> AcknowledgeBuyTransaction(BuyTransaction buyTransaction) {
        // TODO:
        //  validate the received buy or sell transaction
        //  store the transaction info in a db
        return this;
    }


    @Override
    public Receive<Transaction> createReceive() {
        return newReceiveBuilder()
                .onMessage(BuyTransaction.class, this::AcknowledgeBuyTransaction).build();
    }
}