package com.happy.ecofied.gson.travellogdetails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class TravelLogstatus {

    @SerializedName("status")
    @Expose
    private Integer status;
    @SerializedName("statusMessage")
    @Expose
    private String msg;
    @SerializedName("employeeCode")
    @Expose
    private String employeeCode;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

}


