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
    private Double totalAmount;
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
    private Double orderQuantity;
    @SerializedName("idPurchaseOrderHeader")
    @Expose
    private Integer idPurchaseOrderHeader;

    @SerializedName("acceptedQuantity")
    @Expose
    private double acceptedQuantity;

    @SerializedName("idItem")
    @Expose
    private Integer idItem;

    @SerializedName("idUnit")
    @Expose
    private Integer idUnit;

    @SerializedName("idStatus")
    @Expose
    private Integer idStatus;

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

    public Double getOrderQuantity() {
        return orderQuantity;
    }

    public void setOrderQuantity(Double orderQuantity) {
        this.orderQuantity = orderQuantity;
    }

    public Integer getIdPurchaseOrderHeader() {
        return idPurchaseOrderHeader;
    }

    public void setIdPurchaseOrderHeader(Integer idPurchaseOrderHeader) {
        this.idPurchaseOrderHeader = idPurchaseOrderHeader;
    }

    public double getAcceptedQuantity() {
        return acceptedQuantity;
    }

    public void setAcceptedQuantity(double acceptedQuantity) {
        this.acceptedQuantity = acceptedQuantity;
    }

    public Integer getIdStatus() {
        return idStatus;
    }

    public void setIdStatus(Integer idStatus) {
        this.idStatus = idStatus;
    }

    public Integer getIdUnit() {
        return idUnit;
    }

    public void setIdUnit(Integer idUnit) {
        this.idUnit = idUnit;
    }

    public Integer getIdItem() {
        return idItem;
    }

    public void setIdItem(Integer idItem) {
        this.idItem = idItem;
    }
}
