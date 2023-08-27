package com.akkaapp;

import akka.actor.typed.ActorSystem;

import java.util.*;

public class App {

    public static void main(String[] args) {
        ActorSystem<Void> mySystem = ActorSystem.create(FinancialApp.create(), "mySystem");
//        System.out.println(mySystem.printTree());

        try { Thread.sleep(180000); } catch (InterruptedException e) { System.out.println(e); }

        System.out.println(mySystem.printTree());
        mySystem.terminate();

//        HashMap<String, Object> share1 = new HashMap<>();
//        share1.put("company", "NVDA");
//        share1.put("quantity", 1);
//        share1.put("avgPrice", 44.44);
//
//        HashMap<String, Object> share2 = new HashMap<>();
//        share2.put("company", "NVDA");
//        share2.put("quantity", 1);
//        share2.put("avgPrice", 44.44);
//
//        ArrayList<HashMap<String,Object>> shares = new ArrayList<>();
//        shares.add(share1);
//        shares.add(share2);
//
//        System.out.println(shares);
    }
}

