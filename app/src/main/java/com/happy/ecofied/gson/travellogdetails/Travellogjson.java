package com.happy.ecofied.gson.travellogdetails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Travellogjson {

    @SerializedName("data")
    @Expose
    private Data data;
    @SerializedName("errorList")
    @Expose
    private Object errorList;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Object getErrorList() {
        return errorList;
    }

    public void setErrorList(Object errorList) {
        this.errorList = errorList;
    }

}
