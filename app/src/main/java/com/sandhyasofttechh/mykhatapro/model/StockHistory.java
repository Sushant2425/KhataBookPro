package com.sandhyasofttechh.mykhatapro.model;

import java.io.Serializable;

public class StockHistory implements Serializable {

    private String historyId;
    private String productId;
    private String productName;
    private String type; // "IN" or "OUT"
    private String quantity;
    private String price;
    private String date;
    private String time;
    private String note;
    private String oldStock;
    private String newStock;
    private long timestamp;

    public StockHistory() {}

    public StockHistory(String historyId, String productId, String productName, String type,
                        String quantity, String price, String date, String time, String note,
                        String oldStock, String newStock, long timestamp) {
        this.historyId = historyId;
        this.productId = productId;
        this.productName = productName;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.date = date;
        this.time = time;
        this.note = note;
        this.oldStock = oldStock;
        this.newStock = newStock;
        this.timestamp = timestamp;
    }

    // Getters
    public String getHistoryId() {
        return historyId;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getType() {
        return type;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getPrice() {
        return price;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getNote() {
        return note;
    }

    public String getOldStock() {
        return oldStock;
    }

    public String getNewStock() {
        return newStock;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setOldStock(String oldStock) {
        this.oldStock = oldStock;
    }

    public void setNewStock(String newStock) {
        this.newStock = newStock;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    // Helper methods to get double values
    public double getQuantityDouble() {
        try {
            return quantity != null && !quantity.isEmpty() ? Double.parseDouble(quantity) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public double getPriceDouble() {
        try {
            return price != null && !price.isEmpty() ? Double.parseDouble(price) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public double getOldStockDouble() {
        try {
            return oldStock != null && !oldStock.isEmpty() ? Double.parseDouble(oldStock) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public double getNewStockDouble() {
        try {
            return newStock != null && !newStock.isEmpty() ? Double.parseDouble(newStock) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    // Calculate total amount
    public double getTotalAmount() {
        return getQuantityDouble() * getPriceDouble();
    }

    @Override
    public String toString() {
        return "StockHistory{" +
                "historyId='" + historyId + '\'' +
                ", productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", type='" + type + '\'' +
                ", quantity='" + quantity + '\'' +
                ", price='" + price + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", note='" + note + '\'' +
                ", oldStock='" + oldStock + '\'' +
                ", newStock='" + newStock + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}