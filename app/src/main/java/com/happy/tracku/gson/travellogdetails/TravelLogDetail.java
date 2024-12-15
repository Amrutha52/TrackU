package com.happy.tracku.gson.travellogdetails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TravelLogDetail {

    @SerializedName("time")
    @Expose
    private String time;
    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("idEmployee")
    @Expose
    private Integer idEmployee;
    @SerializedName("employeeName")
    @Expose
    private String employeeName;
    @SerializedName("address")
    @Expose
    private String address;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getIdEmployee() {
        return idEmployee;
    }

    public void setIdEmployee(Integer idEmployee) {
        this.idEmployee = idEmployee;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
