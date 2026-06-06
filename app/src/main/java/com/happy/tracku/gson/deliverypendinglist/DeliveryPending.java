package com.happy.tracku.gson.deliverypendinglist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeliveryPending {

    @SerializedName("idSalesHeader")
    @Expose
    private Integer idSalesHeader;
    @SerializedName("vendorName")
    @Expose
    private String vendorName;
    @SerializedName("salesDate")
    @Expose
    private String salesDate;
    @SerializedName("grandTotal")
    @Expose
    private Double grandTotal;
    @SerializedName("itemName")
    @Expose
    private String itemName;
    @SerializedName("quantity")
    @Expose
    private Double quantity;
    @SerializedName("idSalesDetails")
    @Expose
    private Integer idSalesDeliveryDetails;

    @SerializedName("invoiceNumber")
    @Expose
    private String invoiceNumber;
    @SerializedName("assignedEmployee")
    @Expose
    private int assignedEmployee;

    @SerializedName("isCashSale")
    @Expose
    private boolean isCashSale;

    public int getAssignedEmployee() {
        return assignedEmployee;
    }

    public void setAssignedEmployee(int assignedEmployee) {
        this.assignedEmployee = assignedEmployee;
    }

    public boolean getIsCashSale() {
        return isCashSale;
    }

    public void setCashSale(boolean cashSale) {
        isCashSale = cashSale;
    }



    public Integer getIdSalesHeader() {
        return idSalesHeader;
    }

    public void setIdSalesHeader(Integer idSalesHeader) {
        this.idSalesHeader = idSalesHeader;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getSalesDate() {
        return salesDate;
    }

    public void setSalesDate(String salesDate) {
        this.salesDate = salesDate;
    }

    public Double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(Double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Integer getIdSalesDeliveryDetails() {
        return idSalesDeliveryDetails;
    }

    public void setIdSalesDeliveryDetails(Integer idSalesDeliveryDetails) {
        this.idSalesDeliveryDetails = idSalesDeliveryDetails;
    }


    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

}

