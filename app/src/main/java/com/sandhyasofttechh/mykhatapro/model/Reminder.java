package com.sandhyasofttechh.mykhatapro.model;

public class Reminder {
    private String method;
    private long timestamp;


    public Reminder() {}

    public Reminder(String method, long timestamp) {
        this.method = method;
        this.timestamp = timestamp;
    }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}