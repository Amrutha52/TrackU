package com.happy.tracku.gson.masterdata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("vendorMaster")
    @Expose
    private List<VendorMaster> vendorMaster;
    @SerializedName("itemMaster")
    @Expose
    private List<ItemMaster> itemMaster;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;

    public List<VendorMaster> getVendorMaster() {
        return vendorMaster;
    }

    public void setVendorMaster(List<VendorMaster> vendorMaster) {
        this.vendorMaster = vendorMaster;
    }

    public List<ItemMaster> getItemMaster() {
        return itemMaster;
    }

    public void setItemMaster(List<ItemMaster> itemMaster) {
        this.itemMaster = itemMaster;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

}

