package com.kuruvatech.pipeline.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class location {
    public location() {
        type = "";
        coordinates = new ArrayList<LatLng>();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<LatLng> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(ArrayList<LatLng> coordinates) {
        this.coordinates = coordinates;
    }
    String type;
    ArrayList<LatLng> coordinates ;
}
