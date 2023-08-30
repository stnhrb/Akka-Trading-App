package com.akkaapp;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class Auditor extends AbstractBehavior<Auditor.Transaction> {

    public interface Transaction { }
    public static class BuyTransaction implements Transaction {
        public final String companyName;
        public final double price;
        public final String transactionType = "Buy";

        public final ActorRef<Trader.Signal> trader;

        public BuyTransaction(String companyName, double price, ActorRef<Trader.Signal> trader) {
            this.companyName = companyName;
            this.price = price;
            this.trader = trader;
        }
    }

    public static class SellTransaction implements Transaction {
        public final String companyName;
        public final double price;
        public final String transactionType = "Sell";

        public final ActorRef<Trader.Signal> trader;

        public SellTransaction(String companyName, double price, ActorRef<Trader.Signal> trader) {
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
        createSharesTable(databaseConnection);
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
            String sqlStatement = "CREATE TABLE transactions (" +
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

    private void createSharesTable(Connection databaseConnection) {
        try {
            Statement createStatement = databaseConnection.createStatement();
            String sqlStatement = "CREATE TABLE shares (" +
                    "trader VARCHAR ( 100 ) NOT NULL, " +
                    "company VARCHAR ( 10 ) NOT NULL, " +
                    "num_of_shares INTEGER NOT NULL, " +
                    "avg_buy_price NUMERIC(6,4) NOT NULL, " +
                    "last_bought_share TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL, " +
                    "PRIMARY KEY (trader, company));";
            createStatement.executeUpdate(sqlStatement);
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    private boolean insertIntoTransactionDb(Transaction transaction) {
        String traderId = "", transactionType = "", companyName = "";
        double price = 0.0;

        if (transaction instanceof BuyTransaction) {
            traderId = "Trader@" + ((BuyTransaction) transaction).trader.path().uid();
            transactionType = ((BuyTransaction) transaction).transactionType;
            companyName = ((BuyTransaction) transaction).companyName;
            price = ((BuyTransaction) transaction).price;
        } else {
            traderId = "Trader@" + ((SellTransaction) transaction).trader.path().uid();
            transactionType = ((SellTransaction) transaction).transactionType;
            companyName = ((SellTransaction) transaction).companyName;
            price = ((SellTransaction) transaction).price;
        }

        try {
            String sqlStatement = "INSERT INTO public.transactions(" +
                                  "trader, transaction_type, company, price) " +
                                  "VALUES (?, ?, ?, ?);";

            PreparedStatement insertStatement = databaseConnection.prepareStatement(sqlStatement);
            insertStatement.setString(1, traderId);
            insertStatement.setString(2, transactionType);
            insertStatement.setString(3, companyName);
            insertStatement.setDouble(4, price);

            insertStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }

    private boolean insertIntoTSharesDb(BuyTransaction buyTransaction) {
        try {
            String traderId = "Trader@" + buyTransaction.trader.path().uid();
            String companyName = buyTransaction.companyName;
            double avg_buy_price = buyTransaction.price;

            String getSharesSqlStatement = """
                    select (select count(id) from public.transactions where trader = '%1$s' and company = '%2$s' and transaction_type = 'Buy') -
                    (select count(id) from public.transactions where trader = '%1$s' and company = '%2$s' and transaction_type = 'Sell')
                    """.formatted(traderId, companyName);

            String getAvgSqlStatement = "select avg(price) from public.transactions where trader = '" + traderId + "' and company = '"+ companyName +"'";

            String sqlStatement = "INSERT INTO public.shares(" +
                    "trader, company, num_of_shares, avg_buy_price) " +
                    "VALUES ('" + traderId + "', '"+ companyName +"', 1, " + avg_buy_price + ") ON CONFLICT (trader, company) " +
                    "DO UPDATE SET num_of_shares = (" + getSharesSqlStatement + "), avg_buy_price = (" + getAvgSqlStatement + ");";

            Statement statement = databaseConnection.createStatement();
            statement.executeUpdate(sqlStatement);
            return true;
        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }

    private ArrayList<HashMap<String, Object>> returnTraderSharesInfo(ActorRef<Trader.Signal> trader) {
        try {
            String traderId = "Trader@" + trader.path().uid();
            String sqlStatement = """
                    select * from public.shares where trader = '%s'
                    """.formatted(traderId);

            Statement statement = databaseConnection.createStatement();
            ResultSet queryResultSet = statement.executeQuery(sqlStatement);
            ArrayList<HashMap<String, Object>> traderShares = new ArrayList<>();

            while (queryResultSet.next()) {
                HashMap<String, Object> traderShareInfo = new HashMap<>();
                traderShareInfo.put("company", queryResultSet.getString("company"));
                traderShareInfo.put("num_of_shares", queryResultSet.getInt("num_of_shares"));
                traderShareInfo.put("avg_buy_price", queryResultSet.getDouble("avg_buy_price"));
                traderShareInfo.put("last_bought_share", queryResultSet.getTimestamp("last_bought_share"));
                traderShares.add(traderShareInfo);
            }

            return traderShares;
        } catch (SQLException e) {
            System.out.println(e);
            return null;
        }
    }

    private Behavior<Transaction> AcknowledgeBuyTransaction(BuyTransaction buyTransaction) {
        // TODO: validate the received buy or sell transaction

        if (insertIntoTransactionDb(buyTransaction)) { // TODO: insertion to transaction and shares should be an atomic transaction
            insertIntoTSharesDb(buyTransaction);
            ArrayList<HashMap<String, Object>> traderShares = returnTraderSharesInfo(buyTransaction.trader);
            buyTransaction.trader.tell(new Trader.Shares(traderShares));
            System.out.println("Trader@" + buyTransaction.trader.path().uid() + " shares are: " + traderShares);
            System.out.println("Buy Transaction Acknowledged");
        } else
            System.out.println("An error occurred.");
        return this;
    }

    private Behavior<Transaction> AcknowledgeSellTransaction(SellTransaction sellTransaction) {
        if (insertIntoTransactionDb(sellTransaction)) {

        }

        return this;
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

    @Override
    public Receive<Transaction> createReceive() {
        return newReceiveBuilder()
                .onMessage(BuyTransaction.class, this::AcknowledgeBuyTransaction)
                .onMessage(SellTransaction.class, this::AcknowledgeSellTransaction)
                .onSignal(PostStop.class, signal -> onPostStop()).build();
    }
}