package com.happy.ecofied.gson.sendstockoutrequest;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("stockOutSendPurchaseRequestStatus")
    @Expose
    private List<StockOutSendPurchaseRequeststatus> stockOutSendPurchaseRequestStatus;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;

    public List<StockOutSendPurchaseRequeststatus> getStockOutSendPurchaseRequestStatus() {
        return stockOutSendPurchaseRequestStatus;
    }

    public void setStockOutSendPurchaseRequestStatus(List<StockOutSendPurchaseRequeststatus> stockOutSendPurchaseRequestStatus) {
        this.stockOutSendPurchaseRequestStatus = stockOutSendPurchaseRequestStatus;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

}
