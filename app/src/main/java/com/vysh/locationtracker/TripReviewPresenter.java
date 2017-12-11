package com.vysh.locationtracker;

import android.content.Context;
import android.database.Cursor;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vysh.locationtracker.Listeners.TripReviewView;
import com.vysh.locationtracker.Listeners.TripTrackView;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by ramakrishna on 12/2/17.
 */

public class TripReviewPresenter {

    Context context;
    LocalDB localDB;
    TripReviewView tripReviewView;

    public TripReviewPresenter(Context context, TripReviewView tripReviewView) {
        this.context = context;
        localDB = new LocalDB(context);
        this.tripReviewView = tripReviewView;
    }

    public void showMyTripDirections(int tripId) {
        Cursor cursor = localDB.getTripDetails(tripId);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
        } else {
            tripReviewView.onMyTripDetailsEmpty(R.string.msg_current_trip_details_empty);
            return;
        }

        TripModel tripModel = new TripModel();
        tripModel.setTripId(cursor.getInt(cursor.getColumnIndex(LocalDB.FIELD_TRIP_ID)));
        tripModel.setTripStartDate(cursor.getString(cursor.getColumnIndex(LocalDB.FIELD_TRIP_START_DATE)));
        tripModel.setTripEndDate(cursor.getString(cursor.getColumnIndex(LocalDB.FIELD_TRIP_END_DATE)));
        String latLongData = cursor.getString(cursor.getColumnIndex(LocalDB.FIELD_LAT_LNG_DATA));
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<LatLng>>() {
        }.getType();
        ArrayList<LatLng> myTripTrackPoints = gson.fromJson(latLongData, type);
        tripModel.setTripLocationPoints(myTripTrackPoints);

        tripReviewView.onMyTripDetailsFetched(tripModel);
    }
}
