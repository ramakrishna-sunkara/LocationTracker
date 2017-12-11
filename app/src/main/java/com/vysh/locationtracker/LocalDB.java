package com.vysh.locationtracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ramakrishna on 12/2/17.
 */

public class LocalDB extends SQLiteOpenHelper {

    private static String DBNAME = "LocationTrackSQLite";
    private static int VERSION = 1;
    public static final String FIELD_TRIP_ID = "TripId";
    public static final String FIELD_LAT_LNG_DATA = "LngLongData";
    public static final String FIELD_TRIP_START_DATE = "TripStatDate";
    public static final String FIELD_TRIP_END_DATE = "TripEndDate";
    private static final String DATABASE_TABLE = "TripTable";
    private SQLiteDatabase mDB;

    /**
     * Constructor
     */
    public LocalDB(Context context) {
        super(context, DBNAME, null, VERSION);
        this.mDB = getWritableDatabase();
    }


    /**
     * This is a callback method, invoked when the method getReadableDatabase() / getWritableDatabase() is called
     * provided the database does not exists
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + DATABASE_TABLE + " ( " +
                FIELD_TRIP_ID + " integer primary key autoincrement , " +
                FIELD_LAT_LNG_DATA + " text , " +
                FIELD_TRIP_START_DATE + " text , " +
                FIELD_TRIP_END_DATE + " text " +
                " ) ";

        db.execSQL(sql);
    }


    public Cursor getTripDetails(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(DATABASE_TABLE, new String[]{FIELD_TRIP_ID, FIELD_TRIP_START_DATE, FIELD_TRIP_END_DATE, FIELD_LAT_LNG_DATA}, FIELD_TRIP_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        return cursor;
    }

    /**
     * Inserts a new location to the table locations
     */
    public long insert(ContentValues contentValues) {
        long rowID = mDB.insert(DATABASE_TABLE, null, contentValues);
        return rowID;
    }


    /**
     * Deletes all locations from the table
     */
    public int del() {
        int cnt = mDB.delete(DATABASE_TABLE, null, null);
        return cnt;
    }


    /**
     * Returns all the locations from the table
     */
    public Cursor loadAllTrips() {
        return mDB.query(DATABASE_TABLE, new String[]{FIELD_TRIP_ID, FIELD_TRIP_START_DATE, FIELD_TRIP_END_DATE, FIELD_LAT_LNG_DATA},
                null, null, null, null, FIELD_TRIP_ID+" DESC");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void deleteTrip(int tripId) {
        mDB.delete(DATABASE_TABLE, FIELD_TRIP_ID + "=?", new String[]{String.valueOf(tripId)});
    }
}
