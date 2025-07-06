package com.happy.ecofied.gson.photopunchhistoryjson;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("getPunchHistoryDetails")
    @Expose
    private List<GetPunchHistoryDetail> getPunchHistoryDetails;
    @SerializedName("getPunchHistoryStatus")
    @Expose
    private List<GetPunchHistorystatus> getPunchHistoryStatus;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;
    @SerializedName("compressedData")
    @Expose
    private Object compressedData;

    public List<GetPunchHistoryDetail> getGetPunchHistoryDetails() {
        return getPunchHistoryDetails;
    }

    public void setGetPunchHistoryDetails(List<GetPunchHistoryDetail> getPunchHistoryDetails) {
        this.getPunchHistoryDetails = getPunchHistoryDetails;
    }

    public List<GetPunchHistorystatus> getGetPunchHistoryStatus() {
        return getPunchHistoryStatus;
    }

    public void setGetPunchHistoryStatus(List<GetPunchHistorystatus> getPunchHistoryStatus) {
        this.getPunchHistoryStatus = getPunchHistoryStatus;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

    public Object getCompressedData() {
        return compressedData;
    }

    public void setCompressedData(Object compressedData) {
        this.compressedData = compressedData;
    }

}
