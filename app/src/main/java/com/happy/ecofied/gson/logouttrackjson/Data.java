package com.happy.ecofied.gson.logouttrackjson;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("logoutTrackStatus")
    @Expose
    private List<LogoutTrackstatus> logoutTrackStatus;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;

    public List<LogoutTrackstatus> getLogoutTrackStatus() {
        return logoutTrackStatus;
    }

    public void setLogoutTrackStatus(List<LogoutTrackstatus> logoutTrackStatus) {
        this.logoutTrackStatus = logoutTrackStatus;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

}
