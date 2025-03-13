package com.happy.tracku.gson.purchaseorderitemlist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PurchaseOrderItem {

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
    private Integer totalAmount;
    @SerializedName("itemCode")
    @Expose
    private String itemCode;
    @SerializedName("itemName")
    @Expose
    private String itemName;
    @SerializedName("warehouse")
    @Expose
    private String warehouse;
    @SerializedName("floor")
    @Expose
    private String floor;
    @SerializedName("rackNumber")
    @Expose
    private Integer rackNumber;
    @SerializedName("orderQuantity")
    @Expose
    private Integer orderQuantity;
    @SerializedName("idPurchaseOrderHeader")
    @Expose
    private Integer idPurchaseOrderHeader;

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

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public Integer getRackNumber() {
        return rackNumber;
    }

    public void setRackNumber(Integer rackNumber) {
        this.rackNumber = rackNumber;
    }

    public Integer getOrderQuantity() {
        return orderQuantity;
    }

    public void setOrderQuantity(Integer orderQuantity) {
        this.orderQuantity = orderQuantity;
    }

    public Integer getIdPurchaseOrderHeader() {
        return idPurchaseOrderHeader;
    }

    public void setIdPurchaseOrderHeader(Integer idPurchaseOrderHeader) {
        this.idPurchaseOrderHeader = idPurchaseOrderHeader;
    }

}
