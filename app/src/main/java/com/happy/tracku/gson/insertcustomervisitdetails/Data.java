package com.happy.tracku.gson.insertcustomervisitdetails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("customerVisitDetailsStatus")
    @Expose
    private List<CustomerVisitDetailsstatus> customerVisitDetailsStatus;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;

    public List<CustomerVisitDetailsstatus> getCustomerVisitDetailsStatus() {
        return customerVisitDetailsStatus;
    }

    public void setCustomerVisitDetailsStatus(List<CustomerVisitDetailsstatus> customerVisitDetailsStatus) {
        this.customerVisitDetailsStatus = customerVisitDetailsStatus;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

}
