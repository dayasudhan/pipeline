package com.kuruvatech.pipeline.model;
import com.google.android.gms.maps.model.LatLng;

public class GeoPoint {
    LatLng latlng;

    public LatLng getLatlng() {
        return latlng;
    }

    public void setLatlng(LatLng latlng) {
        this.latlng = latlng;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }




    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    double elevation;

    String name;
    public GeoPoint(LatLng ll, double ele, String n) {
        latlng = ll;
        elevation = ele;

        name =  n;
    }

}
