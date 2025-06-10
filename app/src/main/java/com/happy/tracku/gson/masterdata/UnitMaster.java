package com.happy.tracku.gson.masterdata;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class UnitMaster {

    @SerializedName("idUnit")
    @Expose
    private Integer idUnit;
    @SerializedName("unit")
    @Expose
    private String unitName;

    public Integer getIdUnit() {
        return idUnit;
    }

    public void setIdUnit(Integer idUnit) {
        this.idUnit = idUnit;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    @NonNull
    @Override
    public String toString()
    {
        return unitName;
    }
}