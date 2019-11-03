package com.kuruvatech.pipeline.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class location {
    public location() {
        type = "";
        coordinates = new ArrayList<LatLng>();
        name = "";
        phone = "";
        paid = false;
        vendorusername="";
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
    String type;
    ArrayList<LatLng> coordinates ;
    String name;
    String phone;

    public String getVendorusername() {
        return vendorusername;
    }

    public void setVendorusername(String vendorusername) {
        this.vendorusername = vendorusername;
    }

    String vendorusername;
    boolean paid;

}
