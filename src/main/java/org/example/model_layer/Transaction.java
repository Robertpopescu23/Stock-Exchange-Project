package org.example.model_layer;

import java.time.LocalDateTime;

public interface Transaction {
    public String getBuyerId();
    public String getSellerId();
    public Company getCompany();
    public int getQuantity();
    public double getPricePerShare();
    public LocalDateTime getTimestamp();
}