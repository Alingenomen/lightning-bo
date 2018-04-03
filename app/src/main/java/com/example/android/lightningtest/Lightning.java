package com.example.android.lightningtest;

import com.google.android.gms.maps.model.Marker;

import java.util.Date;

/**
 * Created by DTPAdmin on 3/04/2018.
 */

public class Lightning {

    private Date timestamp;
    private Double latitude;
    private Double longitude;
    private Marker lightningMarker;

    public Lightning(Date time, Double lat, Double lon, Marker marker){
        timestamp = time;
        latitude = lat;
        longitude = lon;
        lightningMarker = marker;
    }

    public Date getTimeStamp(){
        return timestamp;
    }

    public Double getLatitude(){
        return latitude;
    }

    public Double getLongitude(){
        return longitude;
    }

    public Marker getLightningMarker(){
        return lightningMarker;
    }

}
