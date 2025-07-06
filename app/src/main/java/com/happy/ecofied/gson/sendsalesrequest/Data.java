package com.happy.ecofied.gson.sendsalesrequest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("sendSalesRequestStatus")
    @Expose
    private List<SendSalesRequeststatus> sendSalesRequestStatus;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;

    public List<SendSalesRequeststatus> getSendSalesRequestStatus() {
        return sendSalesRequestStatus;
    }

    public void setSendSalesRequestStatus(List<SendSalesRequeststatus> sendSalesRequestStatus) {
        this.sendSalesRequestStatus = sendSalesRequestStatus;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

}

