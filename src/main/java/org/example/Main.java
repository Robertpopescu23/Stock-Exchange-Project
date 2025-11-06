package org.example;

import org.example.engine.Buyer;
import org.example.engine.Seller;
import org.example.engine.TradingEngine;
import org.example.model_layer.Company;
import org.example.model_layer.Transaction;
import org.example.engine.DatabaseManager;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        //Create Companies
        List<Company> companies = new ArrayList<>();
        companies.add(new Company("Apple", "AAPL", 150, 150, 1000));
        companies.add(new Company("Google", "GOOG", 2800, 2800, 1000));
        companies.add(new Company("Tesla", "TSLA", 700, 700, 1000));


//        Creates DBManager
        String mongoURI = "mongodb+srv://robertDB:CEBPproject123@cebp-project.afjjmfq.mongodb.net/TradingDB?retryWrites=true&w=majority&tls=true&appName=CEBP-Project";



//        DatabaseManager dbManager = new DatabaseManager(mongoURI, "TradingDB", "Transactions");

        //Create Trading Engine and Thread
        TradingEngine engine = new TradingEngine(companies, null);
        Thread engineThread = new Thread(engine, "ExchangeEngine");
        engineThread.start(); // start the engine

        //Create Buyers
        Buyer buyer1 = new Buyer("B1", 10000, engine);
        Buyer buyer2 = new Buyer("B2", 15000, engine);

        engine.registerBuyer(buyer1);
        engine.registerBuyer(buyer2);

        Thread tBuyer1 = new Thread(buyer1);
        Thread tBuyer2 = new Thread(buyer2);

        tBuyer1.start();
        tBuyer2.start();

        //Create Sellers
        Seller seller1 = new Seller("S1", engine, 0);
        Seller seller2 = new Seller("S2", engine, 0);

        // Assign initial shares
        seller1.getPortfolio().put(companies.get(0), 50); // Apple
        seller1.getPortfolio().put(companies.get(1), 30); // Google

        seller2.getPortfolio().put(companies.get(1), 20); // Google
        seller2.getPortfolio().put(companies.get(2), 40); // Tesla

        engine.registerSeller(seller1);
        engine.registerSeller(seller2);

        Thread tSeller1 = new Thread(seller1);
        Thread tSeller2 = new Thread(seller2);

        tSeller1.start();
        tSeller2.start();

        //Run simulation for some time
        try {
            Thread.sleep(15000); // 15 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Stop all threads safely
        buyer1.stop();
        buyer2.stop();
        seller1.stop();
        seller2.stop();
        engine.stop(); // send SYSTEM_STOP signal

        try {
            tBuyer1.join();
            tBuyer2.join();
            tSeller1.join();
            tSeller2.join();
            engineThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        dbManager.close();

        // Print Transactions
        System.out.println("\n--- Transaction History ---");
        for (Transaction t : engine.getTransactionHistory()) {
            System.out.printf("%s bought %d shares of %s from %s at %.2f%n",
                    t.getBuyerId(), t.getQuantity(), t.getCompany().getSymbol(),
                    t.getSellerId(), t.getPricePerShare());
        }

        //Print Buyers
        System.out.println("\n--- Buyers ---");
        System.out.println(buyer1);
        System.out.println(buyer2);

        //Print Sellers
        System.out.println("\n--- Sellers ---");
        System.out.println(seller1);
        System.out.println(seller2);

        //Print Companies
        System.out.println("\n--- Companies ---");
        for (Company c : companies) {
            System.out.println(c);
        }
    }
}
