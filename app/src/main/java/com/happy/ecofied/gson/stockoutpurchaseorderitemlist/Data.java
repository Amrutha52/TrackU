package com.happy.ecofied.gson.stockoutpurchaseorderitemlist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("stockOutPurchaseOrderItemList")
    @Expose
    private List<StockOutPurchaseOrderItem> stockOutPurchaseOrderItemList;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;

    public List<StockOutPurchaseOrderItem> getStockOutPurchaseOrderItemList() {
        return stockOutPurchaseOrderItemList;
    }

    public void setStockOutPurchaseOrderItemList(List<StockOutPurchaseOrderItem> stockOutPurchaseOrderItemList) {
        this.stockOutPurchaseOrderItemList = stockOutPurchaseOrderItemList;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

}

