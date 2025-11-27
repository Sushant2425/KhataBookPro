package com.sandhyasofttechh.mykhatapro.model;

public class Shop {
    private String shopId;
    private String shopName;
    private String createdAt;

    public Shop() { }

    public Shop(String shopId, String shopName, String createdAt) {
        this.shopId = shopId;
        this.shopName = shopName;
        this.createdAt = createdAt;
    }

    public String getShopId() { return shopId; }
    public void setShopId(String shopId) { this.shopId = shopId; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
