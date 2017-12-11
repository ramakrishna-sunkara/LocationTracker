package com.vysh.locationtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vysh.locationtracker.Listeners.TripTrackView;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by ramakrishna on 12/2/17.
 */

public class LocationTrackPresenter {

    Context context;
    LocalDB localDB;
    TripTrackView tripTrackView;
    long currentTripId;

    public LocationTrackPresenter(Context context, TripTrackView tripTrackView) {
     this.context = context;
     localDB = new LocalDB(context);
     this.tripTrackView = tripTrackView;
    }


    public void saveMyTripData(TripModel tripModel) {
        ContentValues contentValues = new ContentValues();
        Gson gson = new Gson();
        contentValues.put(LocalDB.FIELD_LAT_LNG_DATA, gson.toJson(tripModel.getTripLocationPoints()));
        contentValues.put(LocalDB.FIELD_TRIP_START_DATE, tripModel.getTripStartDate());
        contentValues.put(LocalDB.FIELD_TRIP_END_DATE, tripModel.getTripEndDate());
        currentTripId = localDB.insert(contentValues);
        tripTrackView.onMyTripDetailsSaved();
    }

    public void showMyTripDirections() {
        if (currentTripId > 0){
            Cursor cursor = localDB.getTripDetails((int)currentTripId);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
            }else {
                tripTrackView.onMyTripDetailsEmpty(R.string.msg_current_trip_details_empty);
                return;
            }

            TripModel tripModel = new TripModel();
            tripModel.setTripId(cursor.getInt(cursor.getColumnIndex(LocalDB.FIELD_TRIP_ID)));
            tripModel.setTripStartDate(cursor.getString(cursor.getColumnIndex(LocalDB.FIELD_TRIP_START_DATE)));
            tripModel.setTripEndDate(cursor.getString(cursor.getColumnIndex(LocalDB.FIELD_TRIP_END_DATE)));
            String latLongData = cursor.getString(cursor.getColumnIndex(LocalDB.FIELD_LAT_LNG_DATA));
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<LatLng>>(){}.getType();
            ArrayList<LatLng> myTripTrackPoints = gson.fromJson(latLongData, type);
            tripModel.setTripLocationPoints(myTripTrackPoints);

            tripTrackView.onMyTripDetailsFetched(tripModel);
        }else {
            tripTrackView.onMyTripDetailsEmpty(R.string.msg_current_trip_details_empty);
        }
    }
}
