package com.akkaapp;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.sql.*;
import java.util.Properties;

public class Auditor extends AbstractBehavior<Auditor.Transaction> {

    public interface Transaction { }
    public static class BuyTransaction implements Transaction {
        public final String companyName;
        public final Double price;

        public final String transactionType = "Buy";

        public final ActorRef<Trader.Request> trader;

        public BuyTransaction(String companyName, Double price, ActorRef<Trader.Request> trader) {
            this.companyName = companyName;
            this.price = price;
            this.trader = trader;
        }
    }

    private Connection databaseConnection;

    public static Behavior<Transaction> create() {
        return Behaviors.setup(context -> new Auditor(context));
    }

    public Auditor(ActorContext<Transaction> context) {
        super(context);
        databaseConnection = preparedatabaseConnection();
        createTransactionsTable(databaseConnection);
    }

    private Connection preparedatabaseConnection() {
        String url = "jdbc:postgresql://localhost/postgres";
        Properties props = new Properties();
        props.setProperty("user", "postgres");
        props.setProperty("password", "test123");

        try {
            databaseConnection = DriverManager.getConnection(url, props);
        } catch (SQLException e) {
            System.out.println(e);
        }

        return databaseConnection;
    }

    private void createTransactionsTable(Connection databaseConnection) {
        try {
            Statement createStatement = databaseConnection.createStatement();
            String sqlStatement = "CREATE TABLE Transactions (" +
                                    "id serial PRIMARY KEY NOT NULL, " +
                                    "Trader VARCHAR ( 100 ) NOT NULL, " +
                                    "Transaction_type VARCHAR ( 4 ) NOT NULL, " +
                                    "company VARCHAR ( 10 ) NOT NULL, " +
                                    "price NUMERIC(6,4) NOT NULL, " +
                                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL)";
            createStatement.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            System.out.println(e);
        }
    }
    private boolean insertIntoDb(BuyTransaction buyTransaction) {
        try {
            String sqlStatement = "INSERT INTO public.transactions(" +
                                  "trader, transaction_type, company, price) " +
                                  "VALUES (?, ?, ?, ?);";

            PreparedStatement insertStatement = databaseConnection.prepareStatement(sqlStatement);
            insertStatement.setString(1, "Trader@" + buyTransaction.trader.path().uid());
            insertStatement.setString(2, buyTransaction.transactionType);
            insertStatement.setString(3, buyTransaction.companyName);
            insertStatement.setDouble(4, buyTransaction.price);

            insertStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }

    private Behavior<Transaction> AcknowledgeBuyTransaction(BuyTransaction buyTransaction) {
        // TODO: validate the received buy or sell transaction

        if (insertIntoDb(buyTransaction))
            System.out.println("Buy Transaction Acknowledged");
        else
            System.out.println("An error occurred.");
        return this;
    }


    @Override
    public Receive<Transaction> createReceive() {
        return newReceiveBuilder()
                .onMessage(BuyTransaction.class, this::AcknowledgeBuyTransaction)
                .onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private Behavior<Transaction> onPostStop() {
        System.out.println("Closing DB Connection ...");
        try {
            databaseConnection.close();
        } catch (SQLException e) {
            System.out.println(e);
        }
        return this;
    }
}