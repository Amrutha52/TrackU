package com.happy.tracku.gson.logouttrackjson;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LogoutTrackstatus {

    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("statusMessage")
    @Expose
    private String statusMessage;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

}

