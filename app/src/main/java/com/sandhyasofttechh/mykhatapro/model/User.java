package com.sandhyasofttechh.mykhatapro.model;

public class User {
    private String uid;
    private String email;
    private String password;
    private boolean status;

    public User() {
        // Default constructor required for Firebase
    }

    public User(String uid, String email, String password) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.status = true;
    }
    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }
    // Getters and setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
