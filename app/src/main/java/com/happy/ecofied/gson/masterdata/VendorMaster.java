package com.happy.ecofied.gson.masterdata;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VendorMaster {

    @SerializedName("idVendor")
    @Expose
    private Integer idVendor;
    @SerializedName("vendorName")
    @Expose
    private String vendorName;

    public Integer getIdVendor() {
        return idVendor;
    }

    public void setIdVendor(Integer idVendor) {
        this.idVendor = idVendor;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    @NonNull
    @Override
    public String toString() {
        return vendorName;
    }
}

