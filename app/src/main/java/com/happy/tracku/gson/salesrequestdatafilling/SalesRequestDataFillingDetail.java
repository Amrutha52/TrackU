package com.happy.tracku.gson.salesrequestdatafilling;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SalesRequestDataFillingDetail {

    @SerializedName("idVendor")
    @Expose
    private Integer idVendor;
    @SerializedName("vendorName")
    @Expose
    private String vendorName;
    @SerializedName("mobileNumber")
    @Expose
    private String mobileNumber;
    @SerializedName("eMail")
    @Expose
    private String eMail;

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

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

}
