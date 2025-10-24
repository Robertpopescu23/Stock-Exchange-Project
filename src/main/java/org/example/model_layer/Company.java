package org.example.model_layer;
public class Company {
    private String name;
    private String symbol;
    private double basePrice;
    private double currentPrice;
    private int totalShares;

    public Company(String name, String symbol, double basePrice, double currentPrice, int totalShares) {
        this.name = name;
        this.symbol = symbol;
        this.basePrice = basePrice;
        this.currentPrice = currentPrice;
        this.totalShares = totalShares;
    }

    public void updatePrice(double transactionPrice) {
        // Simulate price change within ±5%
        double changePercent = (Math.random() - 0.5) * 0.1;
        double newPrice = currentPrice * (1 + changePercent);

        if(newPrice < 0.1) throw new IllegalPriceException("New Price modification cannot be done, share price has went down");
        currentPrice = newPrice;
    }
    @Override
    public String toString() {
        return String.format("Company{name='%s', symbol='%s', price='%.2f', totalShares=%d}",
                name, symbol, currentPrice, totalShares);
    }

    // Getters and setters
    public String getName() { return name; }
    public String getSymbol() { return symbol; }
    public double getBasePrice() { return basePrice; }
    public double getCurrentPrice() { return currentPrice; }
    public int getTotalShares() { return totalShares; }
}