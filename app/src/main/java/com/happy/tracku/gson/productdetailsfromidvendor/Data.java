package com.happy.tracku.gson.productdetailsfromidvendor;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("productDetailsResponse")
    @Expose
    private List<ProductDetailsResponse> productDetailsResponse;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;

    public List<ProductDetailsResponse> getProductDetailsResponse() {
        return productDetailsResponse;
    }

    public void setProductDetailsResponse(List<ProductDetailsResponse> productDetailsResponse) {
        this.productDetailsResponse = productDetailsResponse;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

}

