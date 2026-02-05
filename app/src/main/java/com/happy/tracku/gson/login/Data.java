package com.happy.tracku.gson.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("validateLoginResponseStatus")
    @Expose
    private List<ValidateLoginResponsestatus> validateLoginResponseStatus;
    @SerializedName("validateLoginResponseEmployeeData")
    @Expose
    private List<ValidateLoginResponseEmployeeDatum> validateLoginResponseEmployeeData;
    @SerializedName("validateLoginResponseVehicle")
    @Expose
    private List<ValidateLoginResponseVehicle> validateLoginResponseVehicle;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;

    public List<ValidateLoginResponsestatus> getValidateLoginResponseStatus() {
        return validateLoginResponseStatus;
    }

    public void setValidateLoginResponseStatus(List<ValidateLoginResponsestatus> validateLoginResponseStatus) {
        this.validateLoginResponseStatus = validateLoginResponseStatus;
    }

    public List<ValidateLoginResponseEmployeeDatum> getValidateLoginResponseEmployeeData() {
        return validateLoginResponseEmployeeData;
    }

    public void setValidateLoginResponseEmployeeData(List<ValidateLoginResponseEmployeeDatum> validateLoginResponseEmployeeData) {
        this.validateLoginResponseEmployeeData = validateLoginResponseEmployeeData;
    }

    public List<ValidateLoginResponseVehicle> getValidateLoginResponseVehicle() {
        return validateLoginResponseVehicle;
    }

    public void setValidateLoginResponseVehicle(List<ValidateLoginResponseVehicle> validateLoginResponseVehicle) {
        this.validateLoginResponseVehicle = validateLoginResponseVehicle;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

}
