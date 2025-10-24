package org.example.engine;


import org.example.model_layer.Company;
import org.example.model_layer.ShareOffer;
import org.example.model_layer.Transaction;
import org.example.model_layer.TransactionImpl;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

//Manages: offers, requests, matching logic
public class TradingEngine {
    private final List<ShareOffer> buyOrders = new ArrayList<>();
    private final List<ShareOffer> sellOrders = new ArrayList<>();
    private final List<Transaction> transactionHistory = new ArrayList<>();
    private final List<Company> companies;
    private final Map<String, Buyer> buyers = new HashMap<>();
    private final Map<String, Seller> sellers = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Random random = new Random();

    public TradingEngine(List<Company> companies) {
        this.companies = companies;
    }

    public void registerBuyer(Buyer buyer) {
        buyers.put(buyer.getId(), buyer);
    }

    public void registerSeller(Seller seller) {
        sellers.put(seller.getId(), seller);
    }

    public Company getRandomCompany() {
        if (companies.isEmpty()) return null;
        return companies.get(random.nextInt(companies.size()));
    }

    // ---- Order Submission ----
    public void submitBuyOrder(ShareOffer offer) {
        lock.lock();
        try {
            buyOrders.add(offer);
            buyOrders.sort(Comparator.comparingDouble(ShareOffer::getPricePerShare).reversed());
            matchOrders();
        } finally {
            lock.unlock();
        }
    }

    public void submitSellOrder(ShareOffer offer) {
        lock.lock();
        try {
            sellOrders.add(offer);
            sellOrders.sort(Comparator.comparingDouble(ShareOffer::getPricePerShare));
            matchOrders();
        } finally {
            lock.unlock();
        }
    }

    // ---- Matching Logic ----
    private void matchOrders() {
        Iterator<ShareOffer> buyIter = buyOrders.iterator();

        while (buyIter.hasNext()) {
            ShareOffer buy = buyIter.next();
            Iterator<ShareOffer> sellIter = sellOrders.iterator();

            while (sellIter.hasNext()) {
                ShareOffer sell = sellIter.next();

                // Match only if same company and buy price >= sell price
                if (!buy.getCompany().equals(sell.getCompany())) continue;
                if (buy.getPricePerShare() < sell.getPricePerShare()) continue;

//                n = min(offer quantity, request quantity)
                int tradedQuantity = Math.min(buy.getQuantity(), sell.getQuantity());
                double transactionPrice = (buy.getPricePerShare() + sell.getPricePerShare()) / 2;
                double totalValue = tradedQuantity * transactionPrice;

                Buyer buyer = buyers.get(buy.getClientId());
                Seller seller = sellers.get(sell.getClientId());
                if (buyer != null) buyer.updateAfterTransaction(buy.getCompany(), tradedQuantity, totalValue);
                if (seller != null) seller.updateAfterTransaction(buy.getCompany(), tradedQuantity, totalValue);

                // Create a TransactionImpl object
                Transaction transaction = new TransactionImpl(
                        buy.getClientId(),
                        sell.getClientId(),
                        buy.getCompany(),
                        tradedQuantity,
                        transactionPrice,
                        LocalDateTime.now()
                );

                transactionHistory.add(transaction);
                buy.getCompany().updatePrice(transactionPrice);

                buy.reduceQuantity(tradedQuantity);
                sell.reduceQuantity(tradedQuantity);

                if (buy.getQuantity() == 0) buyIter.remove();
                if (sell.getQuantity() == 0) sellIter.remove();

                break; // only one match per iteration
            }
        }
    }
    public List<Transaction> getTransactionHistory() {
        return Collections.unmodifiableList(transactionHistory);
    }
    public List<ShareOffer> getBuyOrders() {
        return Collections.unmodifiableList(buyOrders);
    }
    public List<ShareOffer> getSellOrders() {
        return Collections.unmodifiableList(sellOrders);
    }
}
