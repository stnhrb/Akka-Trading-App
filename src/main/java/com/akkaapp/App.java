package com.akkaapp;

import akka.actor.typed.ActorSystem;

import java.util.*;

public class App {

    public static void main(String[] args) {
        ActorSystem<Void> mySystem = ActorSystem.create(FinancialApp.create(), "mySystem");

        try { Thread.sleep(180000); } catch (InterruptedException e) { System.out.println(e); }

        System.out.println(mySystem.printTree());
        mySystem.terminate();
    }
}

