package com.happy.tracku.gson.employeemasterdetails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class WareHouseMasterDetail
{
    @SerializedName("idWarehouse")
    @Expose
    private Integer idWarehouse;
    @SerializedName("warehouse")
    @Expose
    private String warehouse;
    public Integer getIdWarehouse() {
        return idWarehouse;
    }

    public void setIdWarehouse(Integer idWarehouse) {
        this.idWarehouse = idWarehouse;
    }

    public String getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(String warehouse) {
        this.warehouse = warehouse;
    }


    @Override
    public String toString() {
        return warehouse + '-' + warehouse;
//       return "employeeMaster{" +
//                "employeeCode=" + employeeCode + '-' +
//                ", employeeName='" + employeeName +
//
//                '}';


    }
}