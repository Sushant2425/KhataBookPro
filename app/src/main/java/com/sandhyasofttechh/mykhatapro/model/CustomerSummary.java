package com.sandhyasofttechh.mykhatapro.model;


public class CustomerSummary {
    private String customerName;
    private String customerPhone;
    private double totalGave; // You gave to this customer (money you will get back)
    private double totalGot;  // You got from this customer (money you will give back)
    private double netBalance;
    private long lastTransactionTimestamp;
    private String lastTransactionDate; // <-- ADDED

    public CustomerSummary(String customerName, String customerPhone) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;
    }

    public void addTransaction(Transaction transaction) {
        if ("gave".equals(transaction.getType())) {
            this.totalGave += transaction.getAmount();
        } else {
            this.totalGot += transaction.getAmount();
        }

        // Update last transaction details only if the new one is more recent
        if (transaction.getTimestamp() > this.lastTransactionTimestamp) {
            this.lastTransactionTimestamp = transaction.getTimestamp();
            this.lastTransactionDate = transaction.getDate(); // <-- ADDED
        }
        updateNetBalance();
    }

    private void updateNetBalance() {
        // Net balance from your perspective: (Money you are owed) - (Money you owe)
        this.netBalance = totalGave - totalGot;
    }

    // --- Getters ---
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public double getTotalGave() { return totalGave; }
    public double getTotalGot() { return totalGot; }
    public double getNetBalance() { return netBalance; }
    public long getLastTransactionTimestamp() { return lastTransactionTimestamp; }
    public String getLastTransactionDate() { return lastTransactionDate; } // <-- ADDED
}