package org.example.model_layer;

import java.time.LocalDateTime;
import java.util.UUID;


public class ShareOffer {
    private final String offerId;
    private final String clientId; // Buyer or Seller
    private final Company company;
    private int quantity;
    private double pricePerShare;
    private boolean isBuyOffer;
    private LocalDateTime timestamp;

    public ShareOffer(String clientId, Company company, int quantity, double pricePerShare, boolean isBuyOffer) {
        this.offerId = UUID.randomUUID().toString();
        this.clientId = clientId;
        this.company = company;
        this.quantity = quantity;
        this.pricePerShare = pricePerShare;
        this.isBuyOffer = isBuyOffer;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getOfferId() { return offerId; }
    public String getClientId() { return clientId; }
    public Company getCompany() { return company; }
    public int getQuantity() { return quantity; }
    public double getPricePerShare() { return pricePerShare; }
    public boolean isBuyOffer() { return isBuyOffer; }
    public LocalDateTime getTimestamp() { return timestamp; }

//    Update remaining quantity (used after partial trades)
    public void reduceQuantity(int amount) {
        this.quantity = Math.max(0, this.quantity - amount);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s %d x %.2f (%s)",
                company.getSymbol(),
                isBuyOffer ? "BUY" : "SELL",
                quantity,
                pricePerShare,
                clientId);
    }
    public boolean isBuy() {return isBuyOffer;}
//    public boolean isSell() {return !isBuyOffer;}
}
