package com.sandhyasofttechh.mykhatapro.utils;

import com.sandhyasofttechh.mykhatapro.model.Transaction;
import java.util.List;

/**
 * A utility class to perform calculations on a list of transactions.
 * This is optimized and provides a clean separation of logic from the UI.
 */
public class TransactionCalculator {

    /**
     *
     * A simple data class to hold the results of the calculation.
     */
    public static class Summary {
        public final double totalGot;
        public final double totalGave;
        public final double balance;

        public Summary(double totalGot, double totalGave, double balance) {
            this.totalGot = totalGot;
            this.totalGave = totalGave;
            this.balance = balance;
        }
    }

    /**
     * Calculates the total credit, debit, and final balance from a list of transactions.
     * This is the ready-to-use formula.
     *
     * @param transactions The list of all transactions to process.
     * @return A Summary object containing the calculated totals.
     */
    public static Summary calculate(List<Transaction> transactions) {
        // Using a simple loop is highly efficient (O(n) complexity).
        double totalGot = 0;
        double totalGave = 0;

        if (transactions == null || transactions.isEmpty()) {
            return new Summary(0, 0, 0);
        }

        for (Transaction transaction : transactions) {
            if ("got".equals(transaction.getType())) {
                totalGot += transaction.getAmount();
            } else { // "gave"
                totalGave += transaction.getAmount();
            }
        }

        double balance = totalGot - totalGave;
        return new Summary(totalGot, totalGave, balance);
    }
}