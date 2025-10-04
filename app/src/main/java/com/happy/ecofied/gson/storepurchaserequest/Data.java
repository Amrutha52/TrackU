package com.happy.ecofied.gson.storepurchaserequest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("storePurchaseRequest")
    @Expose
    private List<StorePurchaseRequest> storePurchaseRequest;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;

    public List<StorePurchaseRequest> getStorePurchaseRequest() {
        return storePurchaseRequest;
    }

    public void setStorePurchaseRequest(List<StorePurchaseRequest> storePurchaseRequest) {
        this.storePurchaseRequest = storePurchaseRequest;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

}
