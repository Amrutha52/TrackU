package com.happy.tracku.gson.masterdata;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ItemMaster {

    @SerializedName("idItem")
    @Expose
    private Integer idItem;
    @SerializedName("itemName")
    @Expose
    private String itemName;

    public Integer getIdItem() {
        return idItem;
    }

    public void setIdItem(Integer idItem) {
        this.idItem = idItem;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

}
