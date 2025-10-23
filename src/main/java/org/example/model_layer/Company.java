package org.example.model_layer;

public class Company {
    private final String symbol; //"AAPL"
    private final String name; //Apple Inc.

    public Company(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public String getSymbol() { return symbol; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return symbol + " (" + name + " )";
    }
}