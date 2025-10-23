package org.example.model_layer;

public class ShareOrder {
    public enum Type { BUY, SELL}

    private final Type type;
    private final Company company;
    private final int quantity; //number of shares
    private final double price; //price per share
    private final String traderName;

    public ShareOrder(Type type, Company company, int quantity, double price, String traderName)
    {
        this.type = type;
        this.company = company;
        this.quantity = quantity;
        this.price = price;
        this.traderName = traderName;
    }

    public Type getType() { return type; }
    public Company getCompany() { return company; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public String getTraderName() { return traderName; }

    @Override
    public String toString() {
        return type + " " + quantity + " shares of " + company.getSymbol() + " @ " + price + " by " + traderName;
    }

}
