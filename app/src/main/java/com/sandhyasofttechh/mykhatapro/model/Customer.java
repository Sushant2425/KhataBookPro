package com.sandhyasofttechh.mykhatapro.model;

import java.io.Serializable;

public class Customer implements Serializable {
    private String customerId;

    private String name, phone, email, address;
    private double pendingAmount = 0.0;
    private boolean hasDuePayment = false;
    
    // **NEWLY ADDED**: This field is for the new dashboard calculations.
    private double balance;

    public Customer() {}

    // Getters & Setters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getPendingAmount() { return pendingAmount; }
    public void setPendingAmount(double pendingAmount) { this.pendingAmount = pendingAmount; }

    public boolean isHasDuePayment() { return hasDuePayment; }
    public void setHasDuePayment(boolean hasDuePayment) { this.hasDuePayment = hasDuePayment; }

    // **NEWLY ADDED**: Getter and setter for the balance field.
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
}