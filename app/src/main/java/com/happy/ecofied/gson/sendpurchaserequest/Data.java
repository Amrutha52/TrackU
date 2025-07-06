package com.happy.ecofied.gson.sendpurchaserequest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("sendPurchaseRequestStatus")
    @Expose
    private List<SendPurchaseRequeststatus> sendPurchaseRequestStatus;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;

    public List<SendPurchaseRequeststatus> getSendPurchaseRequestStatus() {
        return sendPurchaseRequestStatus;
    }

    public void setSendPurchaseRequestStatus(List<SendPurchaseRequeststatus> sendPurchaseRequestStatus) {
        this.sendPurchaseRequestStatus = sendPurchaseRequestStatus;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

}
