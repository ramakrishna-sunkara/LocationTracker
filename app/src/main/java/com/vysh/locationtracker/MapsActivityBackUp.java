package com.vysh.locationtracker;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.vysh.locationtracker.Listeners.TripTrackView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MapsActivityBackUp extends FragmentActivity implements OnMapReadyCallback, LocationListener, TripTrackView {
    final static int PERMISSION_ALL = 1;
    final static String[] PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION};
    private GoogleMap mMap;
    MarkerOptions mo;
    Marker marker;
    LocationManager locationManager;
    Button btnGoNext;

    Location mCurrentLocation;
    String mLastUpdateTime;

    boolean isTripStarted = false;

    private ImageView imgShowTripList, imgStart, imgDirections;
    private LinearLayout llShowTripList, llStart, llDirections;
    private TextView txtStart;

    private String tripStartDate, tripEndDate;
    ArrayList<LatLng> tripTrackPoints = new ArrayList<>();
    LocationTrackPresenter locationTrackPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_my_trip);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        btnGoNext = (Button) findViewById(R.id.btnGoNext);
        mapFragment.getMapAsync(this);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mo = new MarkerOptions().position(new LatLng(0, 0)).title("My Current Location");
        if (Build.VERSION.SDK_INT >= 23 && !isPermissionGranted()) {
            requestPermissions(PERMISSIONS, PERMISSION_ALL);
        } else {
            //requestLocation();
        }
        if (!isLocationEnabled())
            showAlert(1);

        imgStart = (ImageView) findViewById(R.id.imgStart);
        imgDirections = (ImageView) findViewById(R.id.imgDirections);
        imgShowTripList = (ImageView) findViewById(R.id.imgShowTripList);

        llStart = (LinearLayout) findViewById(R.id.llStart);
        llDirections = (LinearLayout) findViewById(R.id.llDirections);
        llShowTripList = (LinearLayout) findViewById(R.id.llShowTripList);

        txtStart = (TextView) findViewById(R.id.txtStart);

        locationTrackPresenter = new LocationTrackPresenter(getApplicationContext(), this);

        llStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap != null) {
                    if (isTripStarted) {
                        imgStart.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_play_arrow_black_24dp));
                        txtStart.setText(getString(R.string.nav_start));
                        tripEndDate = DateFormat.getDateTimeInstance().format(new Date());
                        stopLocationUpdates();
                        saveTripData();
                        isTripStarted = false;
                        Toast.makeText(getApplicationContext(), getString(R.string.msg_trip_completed), Toast.LENGTH_SHORT).show();
                        clearMarkers();
                    } else {
                        imgStart.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_stop_black_24dp));
                        txtStart.setText(getString(R.string.nav_stop));
                        tripStartDate = DateFormat.getDateTimeInstance().format(new Date());
                        startLocationUpdates();
                        isTripStarted = true;
                        Toast.makeText(getApplicationContext(), getString(R.string.msg_trip_started), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        llDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getString(R.string.nav_stop).equals(txtStart.getText().toString())) {
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_please_end_trip), Toast.LENGTH_SHORT).show();
                } else {
                    locationTrackPresenter.showMyTripDirections();
                }
            }
        });

        llShowTripList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapsActivityBackUp.this, TripListActivity.class);
                startActivity(intent);
            }
        });
    }

    private void clearMarkers() {
        mMap.clear();
        tripTrackPoints.clear();
    }

    private void saveTripData() {
        TripModel tripModel = new TripModel();
        tripModel.setTripStartDate(tripStartDate);
        tripModel.setTripEndDate(tripEndDate);
        tripModel.setTripLocationPoints(tripTrackPoints);
        locationTrackPresenter.saveMyTripData(tripModel);
    }

    private void startLocationUpdates() {
        requestLocation();
    }

    private void stopLocationUpdates() {
        locationManager.removeUpdates(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        marker = mMap.addMarker(mo);
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng myCoordinates = new LatLng(location.getLatitude(), location.getLongitude());

        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        tripTrackPoints.add(myCoordinates);

        marker.setPosition(myCoordinates);
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_start));
        marker.setTitle(mLastUpdateTime);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myCoordinates,
                16));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void requestLocation() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        String provider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(provider, 10000, 10, this);
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private boolean isPermissionGranted() {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.v("mylog", "Permission is granted");
            return true;
        } else {
            Log.v("mylog", "Permission not granted");
            return false;
        }
    }

    private void showAlert(final int status) {
        String message, title, btnText;
        if (status == 1) {
            message = "Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                    "use this app";
            title = "Enable Location";
            btnText = "Location Settings";
        } else {
            message = "Please allow this app to access location!";
            title = "Permission access";
            btnText = "Grant";
        }
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(false);
        dialog.setTitle(title)
                .setMessage(message)
                .setPositiveButton(btnText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        if (status == 1) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(myIntent);
                        } else
                            requestPermissions(PERMISSIONS, PERMISSION_ALL);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        finish();
                    }
                });
        dialog.show();
    }

    @Override
    public void onMyTripDetailsFetched(TripModel tripModel) {
        Intent intent = new Intent(MapsActivityBackUp.this, TripReviewActivity.class);
        intent.putExtra(TripReviewActivity.EXTRA_TRIP_ID, tripModel.getTripId());
        startActivity(intent);
    }

    @Override
    public void onMyTripDetailsEmpty(int messageId) {
        Toast.makeText(getApplicationContext(), getString(messageId), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMyTripDetailsSaved() {
        Toast.makeText(getApplicationContext(), getString(R.string.msg_trip_saved), Toast.LENGTH_SHORT).show();
    }
}
