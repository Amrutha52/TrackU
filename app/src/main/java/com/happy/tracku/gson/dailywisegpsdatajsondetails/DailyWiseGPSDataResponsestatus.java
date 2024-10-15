package com.happy.tracku.gson.dailywisegpsdatajsondetails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DailyWiseGPSDataResponsestatus {

    @SerializedName("idLocation")
    @Expose
    private String idLocation;
    @SerializedName("longitude")
    @Expose
    private Double longitude;
    @SerializedName("latitude")
    @Expose
    private Double latitude;
    @SerializedName("address")
    @Expose
    private String address;
    @SerializedName("idEmployee")
    @Expose
    private Integer idEmployee;

    public String getIdLocation() {
        return idLocation;
    }

    public void setIdLocation(String idLocation) {
        this.idLocation = idLocation;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getIdEmployee() {
        return idEmployee;
    }

    public void setIdEmployee(Integer idEmployee) {
        this.idEmployee = idEmployee;
    }

}