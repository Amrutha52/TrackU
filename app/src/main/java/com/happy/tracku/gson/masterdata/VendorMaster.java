package com.happy.tracku.gson.masterdata;

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

}

