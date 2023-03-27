package com.akkaapp;

import akka.actor.typed.ActorSystem;

public class App {
    public static void main(String[] args) {
        ActorSystem<FinancialApp.StartApp> mySystem = ActorSystem.create(FinancialApp.create(), "mySystem");
        mySystem.tell(new FinancialApp.StartApp("MyApp"));
    }
}