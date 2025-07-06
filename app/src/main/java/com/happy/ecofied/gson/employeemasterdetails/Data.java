package com.happy.ecofied.gson.employeemasterdetails;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("employeeMasterDetails")
    @Expose
    private List<EmployeeMasterDetail> employeeMasterDetails;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;

    public List<EmployeeMasterDetail> getEmployeeMasterDetails() {
        return employeeMasterDetails;
    }

    public void setEmployeeMasterDetails(List<EmployeeMasterDetail> employeeMasterDetails) {
        this.employeeMasterDetails = employeeMasterDetails;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

}

