package com.happy.tracku.gson.addemployeejsondetails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("insertEmployeeDetailsStatus")
    @Expose
    private List<InsertEmployeeDetailsstatus> insertEmployeeDetailsStatus;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;

    public List<InsertEmployeeDetailsstatus> getInsertEmployeeDetailsStatus() {
        return insertEmployeeDetailsStatus;
    }

    public void setInsertEmployeeDetailsStatus(List<InsertEmployeeDetailsstatus> insertEmployeeDetailsStatus) {
        this.insertEmployeeDetailsStatus = insertEmployeeDetailsStatus;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

}
