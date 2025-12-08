package com.sandhyasofttechh.mykhatapro.model;

import java.io.Serializable;

public class StockHistory implements Serializable {

    String historyId;
    String productId;
    String productName;
    String type;
    String quantity;
    String price;
    String date;
    String time;
    String note;
    String oldStock;
    String newStock;
    long timestamp;
    String unit;
    String timeAdded;
    String hsn;
    String gst;

    public StockHistory() {}

    // ================= GETTERS ====================

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

    public String getUnit() {
        return unit;
    }

    public String getTimeAdded() {
        return timeAdded;
    }

    public String getHsn() {
        return hsn;
    }

    public String getGst() {
        return gst;
    }


    // ================= SETTERS ====================

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

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setTimeAdded(String timeAdded) {
        this.timeAdded = timeAdded;
    }

    public void setHsn(String hsn) {
        this.hsn = hsn;
    }

    public void setGst(String gst) {
        this.gst = gst;
    }


    // ========== Helper Calculations Optional ==========
    public double getQuantityDouble() {
        try {
            return Double.parseDouble(quantity);
        } catch (Exception e) {
            return 0;
        }
    }

    public double getPriceDouble() {
        try {
            return Double.parseDouble(price);
        } catch (Exception e) {
            return 0;
        }
    }

    public double getTotalAmount() {
        return getQuantityDouble() * getPriceDouble();
    }
}
