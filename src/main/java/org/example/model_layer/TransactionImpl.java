package org.example.model_layer;

import java.time.LocalDateTime;

public class TransactionImpl implements Transaction {
    private final String buyerId;
    private final String sellerId;
    private final Company company;
    private final int quantity;
    private final double pricePerShare;
    private final LocalDateTime timestamp;

    public TransactionImpl(String buyerId, String sellerId,
                           Company company, int quantity,
                           double pricePerShare, LocalDateTime timestamp) {
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.company = company;
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
        this.timestamp = timestamp;
    }

    @Override
    public String getBuyerId() { return buyerId; }

    @Override
    public String getSellerId() { return sellerId; }

    @Override
    public Company getCompany() { return company; }

    @Override
    public int getQuantity() { return quantity; }

    @Override
    public double getPricePerShare() { return pricePerShare; }

    @Override
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("Transaction[%s: %d shares of %s @ %.2f]",
                timestamp, quantity, company.getSymbol(), pricePerShare);
    }
}
