package com.sandhyasofttechh.mykhatapro.model;

public class CollectionModel {

    String name, phone;
    double pendingAmount;

    public CollectionModel() {}

    public CollectionModel(String name, String phone, double pendingAmount) {
        this.name = name;
        this.phone = phone;
        this.pendingAmount = pendingAmount;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public double getPendingAmount() { return pendingAmount; }
}
