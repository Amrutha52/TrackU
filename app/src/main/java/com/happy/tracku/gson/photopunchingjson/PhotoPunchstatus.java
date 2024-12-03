package com.happy.tracku.gson.photopunchingjson;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PhotoPunchstatus {

    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("statusMsg")
    @Expose
    private String statusMsg;

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    @SerializedName("statusMessage")
    @Expose
    private String statusMessage;

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
