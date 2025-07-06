package com.happy.ecofied.models.events;

import com.google.android.gms.maps.model.LatLng;

public class EndJourneyEvent
{

    public String event = "END_JOURNEY";
    private LatLng endJourneyLatLng;

    public LatLng getEndJourneyLatLng()
    {
        return endJourneyLatLng;
    }

    public void setEndJourneyLatLng(LatLng endJourneyLatLng)
    {
        this.endJourneyLatLng = endJourneyLatLng;
    }
}