package com.happy.tracku.gson.updatedeliverystatus;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("deliveryStatusUpdate")
    @Expose
    private List<DeliveryStatusUpdate> deliveryStatusUpdate;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;

    public List<DeliveryStatusUpdate> getDeliveryStatusUpdate() {
        return deliveryStatusUpdate;
    }

    public void setDeliveryStatusUpdate(List<DeliveryStatusUpdate> deliveryStatusUpdate) {
        this.deliveryStatusUpdate = deliveryStatusUpdate;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

}
