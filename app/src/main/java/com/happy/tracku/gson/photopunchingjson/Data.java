package com.happy.tracku.gson.photopunchingjson;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Data {

    @SerializedName("photoPunchStatus")
    @Expose
    private List<PhotoPunchstatus> photoPunchStatus;
    @SerializedName("exceptionData")
    @Expose
    private Object exceptionData;

    public List<PhotoPunchstatus> getPhotoPunchStatus() {
        return photoPunchStatus;
    }

    public void setPhotoPunchStatus(List<PhotoPunchstatus> photoPunchStatus) {
        this.photoPunchStatus = photoPunchStatus;
    }

    public Object getExceptionData() {
        return exceptionData;
    }

    public void setExceptionData(Object exceptionData) {
        this.exceptionData = exceptionData;
    }

}

