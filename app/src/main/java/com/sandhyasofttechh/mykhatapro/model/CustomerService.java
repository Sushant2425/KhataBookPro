package com.sandhyasofttechh.mykhatapro.model;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class CustomerService {

    private final DatabaseReference userCustomersRef;
    private final String userEmail;           // ← ADD THIS
    private final FirebaseDatabase database;  // ← ADD THIS

    public CustomerService(String userEmail) {
        this.userEmail = userEmail.replace(".", ",");
        this.database = FirebaseDatabase.getInstance();  // ← Initialize
        this.userCustomersRef = database.getReference("Khatabook")
                .child(this.userEmail)
                .child("customers");
    }

    // === LOG REMINDER ===
    public void logReminder(String customerId, Reminder reminder) {
        DatabaseReference ref = database.getReference("Khatabook")
                .child(userEmail)                     // ← Now resolved
                .child("customers")
                .child(customerId)
                .child("reminders")
                .push();
        ref.setValue(reminder);
    }

    // === GET CUSTOMERS ===
    public void getCustomers(ValueEventListener listener) {
        userCustomersRef.addValueEventListener(listener);
    }

    // === ADD / UPDATE CUSTOMER ===
    public void addOrUpdateCustomer(Customer customer) {
        String key = sanitizePhone(customer.getPhone());
        userCustomersRef.child(key).setValue(customer);
    }

    // === DELETE CUSTOMER ===
    public void deleteCustomer(String phone) {
        String key = sanitizePhone(phone);
        userCustomersRef.child(key).removeValue();
    }

    // === SANITIZE PHONE (Firebase keys can't have . # $ [ ]) ===
    private String sanitizePhone(String phone) {
        return phone.replace(".", "")
                .replace("#", "")
                .replace("$", "")
                .replace("[", "")
                .replace("]", "")
                .replace("+", "plus_")
                .replace(" ", "");
    }
}