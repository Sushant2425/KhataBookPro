package com.sandhyasofttechh.mykhatapro.model;

import java.io.Serializable;

public class Product implements Serializable {

    private String productId;
    private String name;
    private String unit;
    private String salePrice;
    private String purchasePrice;
    private String openingStock;
    private String currentStock;
    private String lowStockAlert;
    private String hsnCode;
    private String gstRate;
    private String imageUrl;
    private String dateAdded;
    private long timestamp;

    public Product() {}

    // =================== GETTERS ======================

    public String getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public String getSalePrice() {
        return salePrice;
    }

    public double getSalePriceDouble() {
        try {
            return Double.parseDouble(salePrice);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public String getPurchasePrice() {
        return purchasePrice;
    }

    public double getPurchasePriceDouble() {
        try {
            return Double.parseDouble(purchasePrice);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public String getOpeningStock() {
        return openingStock;
    }

    public double getOpeningStockDouble() {
        try {
            return Double.parseDouble(openingStock);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public String getCurrentStock() {
        return currentStock;
    }

    public double getCurrentStockDouble() {
        try {
            return Double.parseDouble(currentStock);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public String getLowStockAlert() {
        return lowStockAlert;
    }

    public double getLowStockAlertDouble() {
        try {
            return Double.parseDouble(lowStockAlert);
        } catch (Exception e) {
            return 0.0;
        }
    }

    public String getHsnCode() {
        return hsnCode;
    }

    public String getGstRate() {
        return gstRate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // =================== SETTERS ======================

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setSalePrice(String salePrice) {
        this.salePrice = salePrice;
    }

    public void setPurchasePrice(String purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public void setOpeningStock(String openingStock) {
        this.openingStock = openingStock;
    }

    public void setCurrentStock(String currentStock) {
        this.currentStock = currentStock;
    }

    public void setLowStockAlert(String lowStockAlert) {
        this.lowStockAlert = lowStockAlert;
    }

    public void setHsnCode(String hsnCode) {
        this.hsnCode = hsnCode;
    }

    public void setGstRate(String gstRate) {
        this.gstRate = gstRate;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getHsn() {
        return getHsnCode();
    }

    public String getGst() {
        return getGstRate();
    }

}
