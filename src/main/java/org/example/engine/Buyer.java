package org.example.engine;

import org.example.model_layer.Company;
import org.example.model_layer.ShareOffer;

import java.util.HashMap;
import java.util.List;
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

                // Get list of available sellers for that company
                List<ShareOffer> offers = engine.getSellOffersForCompany(company);

                if(!offers.isEmpty()) {
                    //Choose cheapeast offer
                    ShareOffer bestOffer = offers.get(0);
                    int quantity = Math.min(bestOffer.getQuantity(), random.nextInt(5) + 1);
                    boolean success = engine.executeManualTrade(this, bestOffer, quantity);

                    if(success) {
                        System.out.printf("Buyer %s manually bought %d of %s from %s @ %.2f%n",
                                id, quantity, company.getSymbol(), bestOffer.getClientId(), bestOffer.getPricePerShare());
                    }

                } else {
                    // No available sellers for this company
                    System.out.printf("Buyer %s found no offers available for company %s%n",
                            id, company.getSymbol());
                    Thread.sleep(1000);
                }

                Thread.sleep(1500 + random.nextInt(2000));
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
