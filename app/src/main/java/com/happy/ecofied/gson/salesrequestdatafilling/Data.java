package com.happy.ecofied.gson.salesrequestdatafilling;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("salesRequestDataFillingDetails")
    @Expose
    private List<SalesRequestDataFillingDetail> salesRequestDataFillingDetails;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;

    public List<SalesRequestDataFillingDetail> getSalesRequestDataFillingDetails() {
        return salesRequestDataFillingDetails;
    }

    public void setSalesRequestDataFillingDetails(List<SalesRequestDataFillingDetail> salesRequestDataFillingDetails) {
        this.salesRequestDataFillingDetails = salesRequestDataFillingDetails;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

}

