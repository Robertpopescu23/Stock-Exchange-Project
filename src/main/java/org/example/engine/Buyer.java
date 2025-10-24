package org.example.engine;

import org.example.model_layer.Company;
import org.example.model_layer.ShareOffer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Buyer implements Runnable {
    private final String id;
    private double balance;
    private final Map<Company, Integer> portfolio; // owned shares per company
    private final Random random;
    private final TradingEngine engine;
    private volatile boolean active = true; // control thread stop

    public Buyer(String id, double balance, TradingEngine engine) {
        this.id = id;
        this.balance = balance;
        this.engine = engine;
        this.portfolio = new HashMap<>();
        this.random = new Random();
    }

    @Override
    public void run() {
        while (active) {
            try {
                Company company = engine.getRandomCompany();
                if (company == null) {
                    Thread.sleep(1000);
                    continue;
                }

                // Decide how many shares to buy and max price
                int quantity = random.nextInt(10) + 1; // buy 1-10 shares
                double maxPrice = company.getCurrentPrice() * (1 + random.nextDouble() * 0.05); // up to +5%

                double totalCost = quantity * maxPrice;
                if (totalCost <= balance) {
                    placeBuyOrder(company, quantity, maxPrice);
                }

                Thread.sleep(1000 + random.nextInt(2000)); // wait 1-3 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void placeBuyOrder(Company company, int quantity, double pricePerShare) {
        ShareOffer offer = new ShareOffer(id, company, quantity, pricePerShare, true);
        engine.submitBuyOrder(offer);
    }

    public void updateAfterTransaction(Company company, int tradedQuantity, double totalValue) {
        // Reduce balance
        balance -= totalValue;

        // Add shares to portfolio
        portfolio.merge(company, tradedQuantity, Integer::sum);
    }

    public void stop() {
        active = false;
    }

    public String getId() {
        return id;
    }

    public double getBalance() {
        return balance;
    }

    public Map<Company, Integer> getPortfolio() {
        return portfolio;
    }

    @Override
    public String toString() {
        return String.format("Buyer{id='%s', balance=%.2f, portfolio=%s}", id, balance, portfolio);
    }
}
