package com.sandhyasofttechh.mykhatapro.model;

import java.io.Serializable;

public class PaymentEntry implements Serializable {
    private String type;
    private double amount;
    private Object date;  // ← CHANGE: Use Object to accept String or Long
    private String note;

    public PaymentEntry() {}

    // Get date as long (safe conversion)
    public long getDate() {
        if (date instanceof Long) {
            return (Long) date;
        } else if (date instanceof String) {
            try {
                return Long.parseLong((String) date);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        return 0L;
    }

    public void setDate(Object date) {
        this.date = date;
    }

    // Other getters/setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}