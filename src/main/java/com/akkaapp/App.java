package com.akkaapp;

import akka.actor.typed.ActorSystem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

public class App {
    public static void main(String[] args) {
        ActorSystem<Void> mySystem = ActorSystem.create(FinancialApp.create(), "mySystem");

//        try { Thread.sleep(10000); } catch (InterruptedException e) { System.out.println(e); }

//        System.out.println(mySystem.printTree());;

//        try { Thread.sleep(20000); } catch (InterruptedException e) { System.out.println(e); }

//        System.out.println(mySystem.printTree());;

//        mySystem.terminate();


        // TODO try timer with schActorTarget class to make it print a msg every x seconds.

    }
}

