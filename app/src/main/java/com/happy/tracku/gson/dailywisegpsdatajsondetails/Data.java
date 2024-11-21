package com.happy.tracku.gson.dailywisegpsdatajsondetails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("getDailyWiseGPSDataResponseStatus")
    @Expose
    private List<DailyWiseGPSDataResponsestatus> dailyWiseGPSDataResponseStatus;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;

    public List<DailyWiseGPSDataResponsestatus> getDailyWiseGPSDataResponseStatus() {
        return dailyWiseGPSDataResponseStatus;
    }

    public void setDailyWiseGPSDataResponseStatus(List<DailyWiseGPSDataResponsestatus> dailyWiseGPSDataResponseStatus) {
        this.dailyWiseGPSDataResponseStatus = dailyWiseGPSDataResponseStatus;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

}
