package com.happy.ecofied.gson.updatedeliverystatus;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeliveryStatusUpdate {

    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("statusMsg")
    @Expose
    private String statusMsg;
    @SerializedName("column1")
    @Expose
    private String column1;

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

    public String getColumn1() {
        return column1;
    }

    public void setColumn1(String column1) {
        this.column1 = column1;
    }

}

