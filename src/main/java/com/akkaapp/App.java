package com.akkaapp;

import akka.actor.typed.ActorSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

public class App {
    public static void main(String[] args) {
//        ActorSystem<Void> mySystem = ActorSystem.create(FinancialApp.create(), "mySystem");
        // TODO try timer with schActorTarget class to make it print a msg every x seconds.

        companeyQoute();
    }

    private static String companeyQoute() {
        try {
            URL url = new URL("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=IBM&apikey=WX1VVKD4QIZQKBHX");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int status = con.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null)
                content.append(inputLine);

            in.close();

            JSONObject jsonObj = new JSONObject(content.toString());

            System.out.println(status);

            JSONObject times = jsonObj.getJSONObject("Time Series (Daily)");

            System.out.println(jsonObj.toString());

        } catch (IOException | JSONException e) {
            System.out.println(e);
        }

        return "";
    }
}