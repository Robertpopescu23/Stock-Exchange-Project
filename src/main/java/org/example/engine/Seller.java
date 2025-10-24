package org.example.engine;

import org.example.model_layer.Company;
import org.example.model_layer.ShareOffer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Seller implements Runnable {
    private final String id;
    private final Map<Company, Integer> portofolio;
    private final TradingEngine engine;
    private final Random random;
    private double balance;
    private volatile boolean active = true;

    public Seller(String id, TradingEngine engine, double initialBalance) {
        this.id = id;
        this.engine = engine;
        this.portofolio = new HashMap<>();
        this.random = new Random();
        this.balance = initialBalance;
    }

    @Override
    public void run() {
        while (active) {
            try {
                if (portofolio.isEmpty()) {
                    Thread.sleep(1000);
                    continue;
                }

                Company[] companies = portofolio.keySet().toArray(new Company[0]);
                Company company = companies[random.nextInt(companies.length)];
                int quantity = random.nextInt(portofolio.get(company)) + 1;
                double price = company.getCurrentPrice() * (0.95 + random.nextDouble() * 0.1);

                placeSellOrder(company, quantity, price);

                Thread.sleep(1000 + random.nextInt(2000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void placeSellOrder(Company company, int quantity, double pricePerShare) {
        ShareOffer offer = new ShareOffer(id, company, quantity, pricePerShare, false);
        engine.submitSellOrder(offer);
    }

    public void stop() {
        active = false;
    }

    public void updateAfterTransaction(Company company, int tradedQuantity, double totalValue) {
        balance += totalValue;
        int ownedShares = portofolio.getOrDefault(company, 0);
        int remainingShares = Math.max(0, ownedShares - tradedQuantity);
        if (remainingShares == 0) {
            portofolio.remove(company);
        } else {
            portofolio.put(company, remainingShares);
        }
    }

    public String getId() { return id; }
    public double getBalance() { return balance; }
    public Map<Company, Integer> getPortfolio() { return portofolio; }

    @Override
    public String toString() {
        return String.format("Seller{id='%s', balance=%.2f, portfolio=%s}", id, balance, portofolio);
    }
}
