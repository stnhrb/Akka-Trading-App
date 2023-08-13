package com.akkaapp;

import akka.actor.typed.ActorSystem;

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

