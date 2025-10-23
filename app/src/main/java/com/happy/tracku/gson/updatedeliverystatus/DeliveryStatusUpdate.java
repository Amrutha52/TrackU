package com.happy.tracku.gson.updatedeliverystatus;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeliveryStatusUpdate {

    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("statusMsg")
    @Expose
    private String statusMsg;
    @SerializedName("column1")
    @Expose
    private String column1;

    @SerializedName("salesDate")
    @Expose
    private String salesDate;

    @SerializedName("invoiceNumber")
    @Expose
    private String invoiceNumber;

    @SerializedName("vendorName")
    @Expose
    private String vendorName;
    @SerializedName("itemsCount")
    @Expose
    private Integer itemsCount;
    @SerializedName("deliveredItemsCount")
    @Expose
    private Integer deliveredItemsCount;

    @SerializedName("deliveryStatus")
    @Expose
    private String deliveryStatus;

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getSalesDate() {
        return salesDate;
    }

    public void setSalesDate(String salesDate) {
        this.salesDate = salesDate;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public Integer getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(Integer itemsCount) {
        this.itemsCount = itemsCount;
    }

    public Integer getDeliveredItemsCount() {
        return deliveredItemsCount;
    }

    public void setDeliveredItemsCount(Integer deliveredItemsCount) {
        this.deliveredItemsCount = deliveredItemsCount;
    }

    public String getAllocatedTo() {
        return allocatedTo;
    }

    public void setAllocatedTo(String allocatedTo) {
        this.allocatedTo = allocatedTo;
    }

    @SerializedName("allocatedTo")
    @Expose
    private String allocatedTo;


    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

    public String getColumn1() {
        return column1;
    }

    public void setColumn1(String column1) {
        this.column1 = column1;
    }

}

