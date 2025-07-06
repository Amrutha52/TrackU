package com.happy.ecofied.gson.travellogdetails;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("travelLogDetails")
    @Expose
    private List<TravelLogDetail> travelLogDetails;
    @SerializedName("travelLogStatus")
    @Expose
    private List<TravelLogstatus> travelLogStatus;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;

    public List<TravelLogDetail> getTravelLogDetails() {
        return travelLogDetails;
    }

    public void setTravelLogDetails(List<TravelLogDetail> travelLogDetails) {
        this.travelLogDetails = travelLogDetails;
    }

    public List<TravelLogstatus> getTravelLogStatus() {
        return travelLogStatus;
    }

    public void setTravelLogStatus(List<TravelLogstatus> travelLogStatus) {
        this.travelLogStatus = travelLogStatus;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

}

