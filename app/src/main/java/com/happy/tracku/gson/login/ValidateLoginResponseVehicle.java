package com.happy.tracku.gson.login;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ValidateLoginResponseVehicle
{

    @SerializedName("idVehicle")
    @Expose
    private Integer idVehicle;
    @SerializedName("vehicleNumber")
    @Expose
    private String vehicleNumber;

    public Integer getIdVehicle() {
        return idVehicle;
    }

    public void setIdVehicle(Integer idVehicle) {
        this.idVehicle = idVehicle;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    @NonNull
    @Override
    public String toString() {
        return vehicleNumber;
    }
}

