package org.example;

import org.example.engine.Buyer;
import org.example.engine.Seller;
import org.example.engine.TradingEngine;
import org.example.model_layer.Company;
import org.example.model_layer.Transaction;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Company> companies = new ArrayList<>();
        companies.add(new Company("Apple", "AAPL", 150, 150, 1000));
        companies.add(new Company("Google", "GOOG", 2800, 2800, 1000));
        companies.add(new Company("Tesla", "TSLA", 700, 700, 1000));

        TradingEngine engine = new TradingEngine(companies);

        Buyer buyer1 = new Buyer("B1", 10000, engine);
        Buyer buyer2 = new Buyer("B2", 15000, engine);

        engine.registerBuyer(buyer1);
        engine.registerBuyer(buyer2);

        Seller seller1 = new Seller("S1", engine, 0);
        Seller seller2 = new Seller("S2", engine, 0);

        seller1.getPortfolio().put(companies.get(0), 50); // Apple
        seller1.getPortfolio().put(companies.get(1), 30); // Google

        seller2.getPortfolio().put(companies.get(1), 20); // Google
        seller2.getPortfolio().put(companies.get(2), 40); // Tesla

        engine.registerSeller(seller1);
        engine.registerSeller(seller2);

        Thread tBuyer1 = new Thread(buyer1);
        Thread tBuyer2 = new Thread(buyer2);
        Thread tSeller1 = new Thread(seller1);
        Thread tSeller2 = new Thread(seller2);

        tBuyer1.start();
        tBuyer2.start();
        tSeller1.start();
        tSeller2.start();

        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        buyer1.stop();
        buyer2.stop();
        seller1.stop();
        seller2.stop();

        try {
            tBuyer1.join();
            tBuyer2.join();
            tSeller1.join();
            tSeller2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // --- 8. Print Transactions ---
        System.out.println("\n--- Transaction History ---");
        for (Transaction t : engine.getTransactionHistory()) {
            System.out.printf("%s bought %d shares of %s from %s at %.2f%n",
                    t.getBuyerId(), t.getQuantity(), t.getCompany().getSymbol(),
                    t.getSellerId(), t.getPricePerShare());
        }

        System.out.println("\n--- Buyers ---");
        System.out.println(buyer1);
        System.out.println(buyer2);

        System.out.println("\n--- Sellers ---");
        System.out.println(seller1);
        System.out.println(seller2);

        System.out.println("\n--- Companies ---");
        for (Company c : companies) {
            System.out.println(c);
        }
    }
}
