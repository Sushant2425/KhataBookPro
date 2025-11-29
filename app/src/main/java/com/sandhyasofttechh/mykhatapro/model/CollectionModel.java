package com.sandhyasofttechh.mykhatapro.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CollectionModel implements Serializable {
    private String name;
    private String phone;
    private double pendingAmount;
    public long dueDate;  // new field for dueDate timestamp

    public CollectionModel(String name, String phone, double pendingAmount) {
        this.name = name;
        this.phone = phone;
        this.pendingAmount = pendingAmount;
        this.dueDate = 0L;
    }

    public CollectionModel(String name, String phone, double pendingAmount, long dueDate) {
        this.name = name;
        this.phone = phone;
        this.pendingAmount = pendingAmount;
        this.dueDate = dueDate;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public double getPendingAmount() { return pendingAmount; }
    public long getDueDate() { return dueDate; }

    public String getFormattedDueDate() {
        if (dueDate == 0L) return "No Date Set";
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date(dueDate));
    }


    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }
    public void setPendingAmount(double pendingAmount) {
        this.pendingAmount = pendingAmount;
    }
}
