package org.example.engine;

import org.example.model_layer.Company;
import org.example.model_layer.ShareOffer;
import org.example.model_layer.Transaction;
import org.example.model_layer.TransactionImpl;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

public class TradingEngine implements Runnable {

    private final DatabaseManager dbManager;
    private final BlockingQueue<ShareOffer> orderQueue = new LinkedBlockingQueue<>();
    private final List<ShareOffer> buyOrders = new ArrayList<>();
    private final List<ShareOffer> sellOrders = new ArrayList<>();
    private final List<Transaction> transactionHistory = new ArrayList<>();
    private final List<Company> companies;
    private final Map<String, Buyer> buyers = new HashMap<>();
    private final Map<String, Seller> sellers = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Random random = new Random();
    private volatile boolean running = true;

    public TradingEngine(List<Company> companies, DatabaseManager dbManager) {
        this.companies = companies;
        this.dbManager = dbManager;
    }

    // ---- Registration ----
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

    public void submitBuyOrder(ShareOffer offer) {
        orderQueue.offer(offer);
    }

    public void submitSellOrder(ShareOffer offer) {
        orderQueue.offer(offer);
    }

    @Override
    public void run() {
        while (running) {
            try {
                ShareOffer offer = orderQueue.take(); // waits until an order arrives

                // stop signal check
                if ("SYSTEM_STOP".equals(offer.getClientId())) {
                    break;
                }

                lock.lock();
                try {
                    if (offer.isBuy()) {
                        buyOrders.add(offer);
                        // sort descending by price (highest first) using simple sort
                        sortBuyOrdersDescendingByPrice();
                    } else {
                        sellOrders.add(offer);
                        // sort ascending by price (lowest first) using simple sort
                        sortSellOrdersAscendingByPrice();
                    }
                    matchOrders();
                } finally {
                    lock.unlock();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stop() {
        running = false;
        // poison pill to unblock queue
        ShareOffer stopSignal = new ShareOffer(
                "SYSTEM_STOP",
                null,
                0,
                0.0,
                true
        );
        orderQueue.offer(stopSignal);
    }

    // --- helper sorts (no Comparator, no streams) ---
    private void sortBuyOrdersDescendingByPrice() {
        // Simple selection-sort style: highest price first
        for (int i = 0; i < buyOrders.size() - 1; i++) {
            int maxIdx = i;
            for (int j = i + 1; j < buyOrders.size(); j++) {
                if (buyOrders.get(j).getPricePerShare() > buyOrders.get(maxIdx).getPricePerShare()) {
                    maxIdx = j;
                }
            }
            if (maxIdx != i) {
                ShareOffer tmp = buyOrders.get(i);
                buyOrders.set(i, buyOrders.get(maxIdx));
                buyOrders.set(maxIdx, tmp);
            }
        }
    }

    private void sortSellOrdersAscendingByPrice() {
        // Simple selection-sort style: lowest price first
        for (int i = 0; i < sellOrders.size() - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < sellOrders.size(); j++) {
                if (sellOrders.get(j).getPricePerShare() < sellOrders.get(minIdx).getPricePerShare()) {
                    minIdx = j;
                }
            }
            if (minIdx != i) {
                ShareOffer tmp = sellOrders.get(i);
                sellOrders.set(i, sellOrders.get(minIdx));
                sellOrders.set(minIdx, tmp);
            }
        }
    }

    private void matchOrders() {
        Iterator<ShareOffer> buyIter = buyOrders.iterator();
        while (buyIter.hasNext()) {
            ShareOffer buy = buyIter.next();
            Iterator<ShareOffer> sellIter = sellOrders.iterator();

            while (sellIter.hasNext()) {
                ShareOffer sell = sellIter.next();

                // ensure valid companies
                if (buy.getCompany() == null || sell.getCompany() == null) continue;
                if (!buy.getCompany().equals(sell.getCompany())) continue;
                if (buy.getPricePerShare() < sell.getPricePerShare()) continue;

                // traded quantity = min of both
                int tradedQuantity = Math.min(buy.getQuantity(), sell.getQuantity());
                double transactionPrice = (buy.getPricePerShare() + sell.getPricePerShare()) / 2.0;
                double totalValue = tradedQuantity * transactionPrice;

                Buyer buyer = buyers.get(buy.getClientId());
                Seller seller = sellers.get(sell.getClientId());

                if (buyer != null)
                    buyer.updateAfterTransaction(buy.getCompany(), tradedQuantity, totalValue);
                if (seller != null)
                    seller.updateAfterTransaction(buy.getCompany(), tradedQuantity, totalValue);

                Transaction transaction = new TransactionImpl(
                        buy.getClientId(),
                        sell.getClientId(),
                        buy.getCompany(),
                        tradedQuantity,
                        transactionPrice,
                        LocalDateTime.now()
                );

                transactionHistory.add(transaction);

                // Persist to DB if available
                if (dbManager != null) {
                    try {
                        Document doc = new Document()
                                .append("buyerId", transaction.getBuyerId())
                                .append("sellerId", transaction.getSellerId())
                                .append("company", transaction.getCompany().getSymbol())
                                .append("quantity", transaction.getQuantity())
                                .append("pricePerShare", transaction.getPricePerShare())
                                .append("timestamp", transaction.getTimestamp().toString());

                        dbManager.getTransactionsCollection().insertOne(doc);
                        System.out.println("[DB] Inserted transaction: " + doc.toJson());
                    } catch (Exception e) {
                        System.err.println("[DB] Failed to insert transaction: " + e.getMessage());
                    }
                }

                // update price on company (simulate)
                buy.getCompany().updatePrice(transactionPrice);

                // reduce quantities and remove fully filled offers
                buy.reduceQuantity(tradedQuantity);
                sell.reduceQuantity(tradedQuantity);

                if (buy.getQuantity() == 0) buyIter.remove();
                if (sell.getQuantity() == 0) sellIter.remove();

                // after processing one sell that matched this buy, break to restart outer loop (preserve priority)
                break;
            }
        }
    }

    // ---- New: returns a copy of current sell offers for a company, sorted ascending by price (cheapest first)
    public List<ShareOffer> getSellOffersForCompany(Company company) {
        lock.lock();
        try {
            List<ShareOffer> filtered = new ArrayList<>();
            if (company == null) return filtered;

            for (ShareOffer s : sellOrders) {
                if (s == null) continue;
                if (s.getCompany() == null) continue;
                if (s.getQuantity() <= 0) continue;
                if (s.getCompany().equals(company)) {
                    filtered.add(s);
                }
            }

            // sort ascending by price without Comparator
            for (int i = 0; i < filtered.size() - 1; i++) {
                int minIdx = i;
                for (int j = i + 1; j < filtered.size(); j++) {
                    if (filtered.get(j).getPricePerShare() < filtered.get(minIdx).getPricePerShare()) {
                        minIdx = j;
                    }
                }
                if (minIdx != i) {
                    ShareOffer tmp = filtered.get(i);
                    filtered.set(i, filtered.get(minIdx));
                    filtered.set(minIdx, tmp);
                }
            }

            return filtered;
        } finally {
            lock.unlock();
        }
    }

    // ---- New: execute a manual trade between a buyer and a chosen sell offer
    public boolean executeManualTrade(Buyer buyer, ShareOffer sellOffer, int quantity) {
        lock.lock();
        try {
            if (buyer == null || sellOffer == null) return false;

            // ensure the sellOffer is still present and has enough quantity
            if (!sellOrders.contains(sellOffer)) return false;
            if (quantity <= 0 || quantity > sellOffer.getQuantity()) return false;

            double totalPrice = quantity * sellOffer.getPricePerShare();
            if (buyer.getBalance() < totalPrice) return false;

            // Find seller
            Seller seller = sellers.get(sellOffer.getClientId());
            if (seller == null) return false;

            // Perform trade: update buyer and seller
            buyer.updateAfterTransaction(sellOffer.getCompany(), quantity, totalPrice);
            seller.updateAfterTransaction(sellOffer.getCompany(), quantity, totalPrice);

            // Reduce the sell offer quantity and remove if empty
            sellOffer.reduceQuantity(quantity);
            if (sellOffer.getQuantity() == 0) {
                // remove the offer from sellOrders
                Iterator<ShareOffer> it = sellOrders.iterator();
                while (it.hasNext()) {
                    ShareOffer s = it.next();
                    if (s.getOfferId().equals(sellOffer.getOfferId())) {
                        it.remove();
                        break;
                    }
                }
            }

            // Record transaction with the sellOffer price as execution price
            Transaction transaction = new TransactionImpl(
                    buyer.getId(),
                    seller.getId(),
                    sellOffer.getCompany(),
                    quantity,
                    sellOffer.getPricePerShare(),
                    LocalDateTime.now()
            );
            transactionHistory.add(transaction);

            // Persist to DB if available
            if (dbManager != null) {
                try {
                    Document doc = new Document()
                            .append("buyerId", transaction.getBuyerId())
                            .append("sellerId", transaction.getSellerId())
                            .append("company", transaction.getCompany().getSymbol())
                            .append("quantity", transaction.getQuantity())
                            .append("pricePerShare", transaction.getPricePerShare())
                            .append("timestamp", transaction.getTimestamp().toString());

                    dbManager.getTransactionsCollection().insertOne(doc);
                    System.out.println("[DB] Inserted manual transaction: " + doc.toJson());
                } catch (Exception e) {
                    System.err.println("[DB] Failed to insert manual transaction: " + e.getMessage());
                }
            }

            // Optionally update company price using the executed price
            if (sellOffer.getCompany() != null) {
                try {
                    sellOffer.getCompany().updatePrice(sellOffer.getPricePerShare());
                } catch (RuntimeException ex) {
                    // if company price update throws, print and continue
                    System.err.println("[Engine] Company price update failed: " + ex.getMessage());
                }
            }

            System.out.println("[Manual Trade] " + transaction);
            return true;
        } finally {
            lock.unlock();
        }
    }

    // ---- Accessors ----
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
