//package com.sandhyasofttechh.mykhatapro.model;
//
//
//public class CustomerSummary {
//    private String customerName;
//    private String customerPhone;
//    private double totalGave; // You gave to this customer (money you will get back)
//    private double totalGot;  // You got from this customer (money you will give back)
//    private double netBalance;
//    private long lastTransactionTimestamp;
//    private String lastTransactionDate; // <-- ADDED
//
//    public CustomerSummary(String customerName, String customerPhone) {
//        this.customerName = customerName;
//        this.customerPhone = customerPhone;
//    }
//
//    public void addTransaction(Transaction transaction) {
//        if ("gave".equals(transaction.getType())) {
//            this.totalGave += transaction.getAmount();
//        } else {
//            this.totalGot += transaction.getAmount();
//        }
//
//        // Update last transaction details only if the new one is more recent
//        if (transaction.getTimestamp() > this.lastTransactionTimestamp) {
//            this.lastTransactionTimestamp = transaction.getTimestamp();
//            this.lastTransactionDate = transaction.getDate(); // <-- ADDED
//        }
//        updateNetBalance();
//    }
//
//    private void updateNetBalance() {
//        // Net balance from your perspective: (Money you are owed) - (Money you owe)
//        this.netBalance = totalGave - totalGot;
//    }
//
//    // --- Getters ---
//    public String getCustomerName() { return customerName; }
//    public String getCustomerPhone() { return customerPhone; }
//    public double getTotalGave() { return totalGave; }
//    public double getTotalGot() { return totalGot; }
//    public double getNetBalance() { return netBalance; }
//    public long getLastTransactionTimestamp() { return lastTransactionTimestamp; }
//    public String getLastTransactionDate() { return lastTransactionDate; } // <-- ADDED
//}



package com.sandhyasofttechh.mykhatapro.model;

import java.util.ArrayList;
import java.util.List;

public class CustomerSummary {

    private String customerName;
    private String customerPhone;
    private double netBalance = 0;
    private String lastTransactionDate = "";
    private long lastTransactionTimestamp = 0;

    // NEW: store list of all transactions (needed for PDF)
    private List<Transaction> transactions = new ArrayList<>();

    public CustomerSummary() {
    }

    public CustomerSummary(String customerName, String customerPhone) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;
    }

    // Add transaction and automatically update summary
    public void addTransaction(Transaction t) {
        if (t == null) return;

        transactions.add(t);

        // Update last transaction timestamp & date
        long ts = t.getTimestamp();
        if (ts > lastTransactionTimestamp) {
            lastTransactionTimestamp = ts;
            lastTransactionDate = t.getDate();
        }

        // Update balance
        if ("GAVE".equalsIgnoreCase(t.getType()) || "PAYMENT".equalsIgnoreCase(t.getType())) {
            netBalance -= t.getAmount(); // customer gave you => you owe them
        } else {
            netBalance += t.getAmount(); // customer got => they owe you
        }
    }

    // Getters
    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public double getNetBalance() {
        return netBalance;
    }

    public String getLastTransactionDate() {
        return lastTransactionDate == null ? "" : lastTransactionDate;
    }

    public long getLastTransactionTimestamp() {
        return lastTransactionTimestamp;
    }

    // NEW: Needed for PDF generation
    public List<Transaction> getTransactions() {
        return transactions;
    }
}
