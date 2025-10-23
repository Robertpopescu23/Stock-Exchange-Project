package org.example.model_layer;

public class Transaction {
    private final Company company;
    private final int quantity;
    private final double price;
    private final String buyer;
    private final String seller;

    public Transaction(Company company, int quantity, double price, String buyer, String seller) {
        this.company = company;
        this.quantity = quantity;
        this.price = price;
        this.buyer = buyer;
        this.seller = seller;
    }

    @Override
    public String toString() {
        return "TRADE: " + quantity + " shares of " + company.getSymbol() + " @ " + price +
                " between " + buyer + " and " + seller;
    }
}
