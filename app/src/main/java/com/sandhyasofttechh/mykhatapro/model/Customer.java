//package com.sandhyasofttechh.mykhatapro.model;
//
//public class Customer {
//    private String name, phone, email, address;
//
//    public Customer() { }
//
//    public Customer(String name, String phone, String email, String address) {
//        this.name = name;
//        this.phone = phone;
//        this.email = email;
//        this.address = address;
//    }
//
//    public String getName() { return name; }
//    public String getPhone() { return phone; }
//    public String getEmail() { return email; }
//    public String getAddress() { return address; }
//
//    public void setName(String name) { this.name = name; }
//    public void setPhone(String phone) { this.phone = phone; }
//    public void setEmail(String email) { this.email = email; }
//    public void setAddress(String address) { this.address = address; }
//}


package com.sandhyasofttechh.mykhatapro.model;

public class Customer implements java.io.Serializable {
    private String customerId;
    private String name, phone, email, address;
    private double pendingAmount = 0.0;
    private boolean hasDuePayment = false;  // NEW: true if "You Gave"

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

    // NEW: For bell icon
    public boolean isHasDuePayment() { return hasDuePayment; }
    public void setHasDuePayment(boolean hasDuePayment) { this.hasDuePayment = hasDuePayment; }
}