package com.happy.tracku.gson.gpsstatusjson;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationUpdatestatus {

    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("statusMsg")
    @Expose
    private String statusMsg;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }

}
