package com.sandhyasofttechh.mykhatapro.model;

import java.io.Serializable;

public class Transaction implements Serializable {
    private String id;
    private String customerPhone;
    private String customerName;
    private double amount;
    private String type; // "gave" or "got"
    private String attachedFileUrl;  // URL of uploaded image/file in Firebase Storage

    private String note;
    private String date;
    private long timestamp;
    private long deletedAt; // timestamp in milliseconds


    public Transaction() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public long getDeletedAt() { return deletedAt; }
    public void setDeletedAt(long deletedAt) { this.deletedAt = deletedAt; }

    public String getAttachedFileUrl() {
        return attachedFileUrl;
    }

    public void setAttachedFileUrl(String attachedFileUrl) {
        this.attachedFileUrl = attachedFileUrl;
    }
}