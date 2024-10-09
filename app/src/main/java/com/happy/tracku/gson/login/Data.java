package com.happy.tracku.gson.login;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("loginResponseStatus")
    @Expose
    private List<LoginResponsestatus> loginResponseStatus;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;
    @SerializedName("token")
    @Expose
    private String token;

    public List<LoginResponsestatus> getLoginResponseStatus() {
        return loginResponseStatus;
    }

    public void setLoginResponseStatus(List<LoginResponsestatus> loginResponseStatus) {
        this.loginResponseStatus = loginResponseStatus;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

}