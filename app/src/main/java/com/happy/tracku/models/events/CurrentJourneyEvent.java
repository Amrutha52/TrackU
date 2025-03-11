package com.happy.tracku.models.events;

import com.google.android.gms.maps.model.LatLng;

public class CurrentJourneyEvent {

    private String event = "BEGIN_JOURNEY";
    private LatLng currentLatLng;

    public LatLng getCurrentLatLng() {
        return currentLatLng;
    }

    public void setCurrentLatLng(LatLng currentLatLng) {
        this.currentLatLng = currentLatLng;
    }
}