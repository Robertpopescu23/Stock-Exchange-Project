package org.example;

import org.example.engine.Buyer;
import org.example.engine.Seller;
import org.example.engine.TradingEngine;
import org.example.model_layer.Company;
import org.example.model_layer.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    private List<Company> companies;
    private TradingEngine engine;
    private Buyer buyer1;
    private Buyer buyer2;
    private Seller seller1;
    private Seller seller2;

    @BeforeEach
    public void setUp() {
        companies = new ArrayList<>();
        companies.add(new Company("Apple", "AAPL", 150, 150, 1000));
        companies.add(new Company("Google", "GOOG", 2800, 2800, 1000));
        companies.add(new Company("Tesla", "TSLA", 700, 700, 1000));

        engine = new TradingEngine(companies);

        buyer1 = new Buyer("B1", 10000, engine);
        buyer2 = new Buyer("B2", 15000, engine);
        engine.registerBuyer(buyer1);
        engine.registerBuyer(buyer2);

        seller1 = new Seller("S1", engine, 0);
        seller2 = new Seller("S2", engine, 0);

        seller1.getPortfolio().put(companies.get(0), 50); // Apple
        seller1.getPortfolio().put(companies.get(1), 30); // Google
        seller2.getPortfolio().put(companies.get(1), 20); // Google
        seller2.getPortfolio().put(companies.get(2), 40); // Tesla

        engine.registerSeller(seller1);
        engine.registerSeller(seller2);
    }

    @Test
    public void testStockExchangeSimulation() throws InterruptedException {
        Thread tBuyer1 = new Thread(buyer1);
        Thread tBuyer2 = new Thread(buyer2);
        Thread tSeller1 = new Thread(seller1);
        Thread tSeller2 = new Thread(seller2);

        tBuyer1.start();
        tBuyer2.start();
        tSeller1.start();
        tSeller2.start();

        Thread.sleep(5000);

        buyer1.stop();
        buyer2.stop();
        seller1.stop();
        seller2.stop();

        tBuyer1.join();
        tBuyer2.join();
        tSeller1.join();
        tSeller2.join();

        List<Transaction> transactions = engine.getTransactionHistory();
        assertFalse(transactions.isEmpty(), "No transactions occurred!");
        assertTrue(buyer1.getBalance() < 10000 || buyer2.getBalance() < 15000,
                "Buyers' balances did not decrease");
        assertTrue(seller1.getBalance() > 0 || seller2.getBalance() > 0,
                "Sellers' balances did not increase");

        boolean priceChanged = companies.stream().anyMatch(c -> c.getCurrentPrice() != c.getBasePrice());
        assertTrue(priceChanged, "Company prices did not change");
    }
}
