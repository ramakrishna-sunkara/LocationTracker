package com.vysh.locationtracker;

import android.content.Context;
import android.database.Cursor;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vysh.locationtracker.Listeners.TripListView;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by ramakrishna on 12/2/17.
 */

public class TripListPresenter {

    Context context;
    LocalDB localDB;
    TripListView tripListView;

    public TripListPresenter(Context context, TripListView tripListView) {
        this.context = context;
        localDB = new LocalDB(context);
        this.tripListView = tripListView;
    }

    public void showAllTrips() {

        ArrayList<TripModel> tripModels;
        Cursor cursor = localDB.loadAllTrips();
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            tripModels = new ArrayList<>();
        } else {
            tripListView.onTripDetailsEmpty();
            return;
        }

        for (int i = 0; i < cursor.getCount(); i++) {
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
            tripModels.add(tripModel);
            cursor.moveToNext();
        }

        tripListView.onTripDetailsFetched(tripModels);
    }

    public void deleteTripId(int tripId) {
        localDB.deleteTrip(tripId);
    }
}
