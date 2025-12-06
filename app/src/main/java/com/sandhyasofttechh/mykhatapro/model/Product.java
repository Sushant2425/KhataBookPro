package com.sandhyasofttechh.mykhatapro.model;

import java.io.Serializable;

public class Product implements Serializable {

    private String productId;
    private String name;
    private String unit;
    private String salePrice;
    private String purchasePrice;
    private String openingStock;
    private String lowStockAlert;
    private String hsn;
    private String gst;
    private String dateAdded;
    private String imageUrl;
    private String hsnCode;
    private String gstRate;


    public Product() {
        // required by Firebase
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }

    public String getUnit() { return unit != null ? unit : ""; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getHsnCode() {
        return hsnCode;
    }

    public void setHsnCode(String hsnCode) {
        this.hsnCode = hsnCode;
    }

    public String getGstRate() {
        return gstRate;
    }

    public void setGstRate(String gstRate) {
        this.gstRate = gstRate;
    }


    public String getSalePrice() { return salePrice != null ? salePrice : "0"; }
    public void setSalePrice(String salePrice) { this.salePrice = salePrice; }

    public String getPurchasePrice() { return purchasePrice != null ? purchasePrice : "0"; }
    public void setPurchasePrice(String purchasePrice) { this.purchasePrice = purchasePrice; }

    public String getOpeningStock() { return openingStock != null ? openingStock : "0"; }
    public void setOpeningStock(String openingStock) { this.openingStock = openingStock; }

    public String getLowStockAlert() { return lowStockAlert != null ? lowStockAlert : "0"; }
    public void setLowStockAlert(String lowStockAlert) { this.lowStockAlert = lowStockAlert; }

    public String getHsn() { return hsn != null ? hsn : ""; }
    public void setHsn(String hsn) { this.hsn = hsn; }

    public String getGst() { return gst != null ? gst : ""; }
    public void setGst(String gst) { this.gst = gst; }

    public String getDateAdded() { return dateAdded != null ? dateAdded : ""; }
    public void setDateAdded(String dateAdded) { this.dateAdded = dateAdded; }

    public String getImageUrl() { return imageUrl != null ? imageUrl : ""; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // Helper for numeric usage
    public double getOpeningStockDouble() {
        try { return Double.parseDouble(getOpeningStock()); } catch (Exception e) { return 0; }
    }

    public double getPurchasePriceDouble() {
        try { return Double.parseDouble(getPurchasePrice()); } catch (Exception e) { return 0; }
    }

    public double getLowStockAlertDouble() {
        try { return Double.parseDouble(getLowStockAlert()); } catch (Exception e) { return 0; }
    }
}
