package com.happy.ecofied.gson.deliverypendinglist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("deliveryPending")
    @Expose
    private List<DeliveryPending> deliveryPending;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;

    public List<DeliveryPending> getDeliveryPending() {
        return deliveryPending;
    }

    public void setDeliveryPending(List<DeliveryPending> deliveryPending) {
        this.deliveryPending = deliveryPending;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

}

