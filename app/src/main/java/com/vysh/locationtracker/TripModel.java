package com.vysh.locationtracker;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by ramakrishna on 12/2/17.
 */

public class TripModel {
    private String tripStartDate,tripEndDate;
    private int tripId;
    private ArrayList<LatLng> tripLocationPoints;

    public String getTripStartDate() {
        return tripStartDate;
    }

    public void setTripStartDate(String tripStartDate) {
        this.tripStartDate = tripStartDate;
    }

    public String getTripEndDate() {
        return tripEndDate;
    }

    public void setTripEndDate(String tripEndDate) {
        this.tripEndDate = tripEndDate;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public ArrayList<LatLng> getTripLocationPoints() {
        return tripLocationPoints;
    }

    public void setTripLocationPoints(ArrayList<LatLng> tripLocationPoints) {
        this.tripLocationPoints = tripLocationPoints;
    }
}
