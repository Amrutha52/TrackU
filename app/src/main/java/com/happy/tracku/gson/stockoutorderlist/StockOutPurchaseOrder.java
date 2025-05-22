package com.happy.tracku.gson.stockoutorderlist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StockOutPurchaseOrder {

    @SerializedName("vendorName")
    @Expose
    private String vendorName;
    @SerializedName("purchaseOrderNumber")
    @Expose
    private String purchaseOrderNumber;
    @SerializedName("purchaseOrderDate")
    @Expose
    private String purchaseOrderDate;
    @SerializedName("totalAmount")
    @Expose
    private Double totalAmount;
    @SerializedName("idSalesHeader")
    @Expose
    private Integer idSalesHeader;

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

    public String getPurchaseOrderDate() {
        return purchaseOrderDate;
    }

    public void setPurchaseOrderDate(String purchaseOrderDate) {
        this.purchaseOrderDate = purchaseOrderDate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getIdSalesHeader() {
        return idSalesHeader;
    }

    public void setIdSalesHeader(Integer idSalesHeader) {
        this.idSalesHeader = idSalesHeader;
    }

}