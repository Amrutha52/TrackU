package com.happy.ecofied.gson.employeemasterdetails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class EmployeeMasterDetail {

    @SerializedName("idEmployee")
    @Expose
    private Integer idEmployee;
    @SerializedName("employeeCode")
    @Expose
    private String employeeCode;
    @SerializedName("employeeName")
    @Expose
    private String employeeName;

    public Integer getIdEmployee() {
        return idEmployee;
    }

    public void setIdEmployee(Integer idEmployee) {
        this.idEmployee = idEmployee;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(String employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }


    @Override
    public String toString() {
        return employeeCode + '-' + employeeName;
//       return "employeeMaster{" +
//                "employeeCode=" + employeeCode + '-' +
//                ", employeeName='" + employeeName +
//
//                '}';


    }
}

