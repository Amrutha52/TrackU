package com.happy.tracku.gson.photopunchhistoryjson;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetPunchHistoryDetail {

    @SerializedName("date")
    @Expose
    private String date;
    @SerializedName("idEmployee")
    @Expose
    private Integer idEmployee;
    @SerializedName("employeeName")
    @Expose
    private String employeeName;
    @SerializedName("punchTime")
    @Expose
    private String punchTime;

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    @SerializedName("employeeCode")
    @Expose
    private String employeeCode;

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

    public String getPunchTime() {
        return punchTime;
    }

    public void setPunchTime(String punchTime) {
        this.punchTime = punchTime;
    }

}

