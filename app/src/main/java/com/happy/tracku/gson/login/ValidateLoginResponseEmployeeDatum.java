package com.happy.tracku.gson.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ValidateLoginResponseEmployeeDatum
{

    @SerializedName("employeeCode")
    @Expose
    private Integer employeeCode;
    @SerializedName("employeeName")
    @Expose
    private String employeeName;

    public Integer getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(Integer employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    @Override
    public String toString()
    {
        return employeeName;
    }
}

