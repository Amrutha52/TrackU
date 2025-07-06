package com.happy.ecofied.models.events;

import com.google.android.gms.maps.model.LatLng;

public class BeginJourneyEvent {

    private String event = "BEGIN_JOURNEY";
    private LatLng beginLatLng;

    public LatLng getBeginLatLng() {
        return beginLatLng;
    }

    public void setBeginLatLng(LatLng beginLatLng) {
        this.beginLatLng = beginLatLng;
    }

}