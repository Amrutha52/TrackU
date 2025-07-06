package com.happy.ecofied.gson.gpsstatusjson;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("updateDailyGPSDataStatus")
    @Expose
    private List<LocationUpdatestatus> locationUpdateStatus;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;

    public List<LocationUpdatestatus> getLocationUpdateStatus() {
        return locationUpdateStatus;
    }

    public void setLocationUpdateStatus(List<LocationUpdatestatus> locationUpdateStatus) {
        this.locationUpdateStatus = locationUpdateStatus;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

}
