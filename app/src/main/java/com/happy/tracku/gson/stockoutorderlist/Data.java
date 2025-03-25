package com.happy.tracku.gson.stockoutorderlist;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("stockOutPurchaseOrderList")
    @Expose
    private List<StockOutPurchaseOrder> stockOutPurchaseOrderList;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;

    public List<StockOutPurchaseOrder> getStockOutPurchaseOrderList() {
        return stockOutPurchaseOrderList;
    }

    public void setStockOutPurchaseOrderList(List<StockOutPurchaseOrder> stockOutPurchaseOrderList) {
        this.stockOutPurchaseOrderList = stockOutPurchaseOrderList;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

}
