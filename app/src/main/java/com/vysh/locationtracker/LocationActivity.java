package com.vysh.locationtracker;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;
import com.vysh.locationtracker.Listeners.TripTrackView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LocationActivity extends AppCompatActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, TripTrackView {

    final static String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION};

    private static final String TAG = "LocationActivity";
    private static final long INTERVAL = 1000 * 5 * 1; //5 sec
    private static final long FASTEST_INTERVAL = 1000 * 5 * 1; // 5 sec
    Button btnFusedLocation;
    TextView tvLocation;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    GoogleMap googleMap;

    boolean isTripStarted = false;

    private ImageView imgShowTripList, imgStart, imgDirections;
    private LinearLayout llShowTripList, llStart, llDirections;
    private TextView txtStart;

    private String tripStartDate, tripEndDate;
    ArrayList<LatLng> tripTrackPoints = new ArrayList<>();
    LocationTrackPresenter locationTrackPresenter;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate ...............................");
        //show error dialog if GoolglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }

        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        setContentView(R.layout.activity_location_google_map);
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        googleMap = fm.getMap();
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        //addCurrentLocationMarker();

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
                if (mGoogleApiClient.isConnected()) {
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
                if (getString(R.string.nav_stop).equals(txtStart.getText().toString())){
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_please_end_trip), Toast.LENGTH_SHORT).show();
                }else {
                    locationTrackPresenter.showMyTripDirections();
                }
            }
        });

        llShowTripList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LocationActivity.this,TripListActivity.class);
                startActivity(intent);
            }
        });
    }

    private void clearMarkers() {
        googleMap.clear();
        tripTrackPoints.clear();
    }

    private void saveTripData() {
        TripModel tripModel = new TripModel();
        tripModel.setTripStartDate(tripStartDate);
        tripModel.setTripEndDate(tripEndDate);
        tripModel.setTripLocationPoints(tripTrackPoints);
        locationTrackPresenter.saveMyTripData(tripModel);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        //mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        //startLocationUpdates();
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        //Toast.makeText(this, "Location Updated...", Toast.LENGTH_SHORT).show();
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        addMarker();
    }

    private void addMarker() {
        MarkerOptions options = new MarkerOptions();

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setStyle(IconGenerator.STYLE_PURPLE);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_start));
        options.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());


        LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        options.position(currentLatLng);
        Marker mapMarker = googleMap.addMarker(options);
        long atTime = mCurrentLocation.getTime();
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date(atTime));
        mapMarker.setTitle(mLastUpdateTime);
        Log.d(TAG, "Marker added.............................");
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,
                16));
        Log.d(TAG, "Zoom done.............................");

        tripTrackPoints.add(currentLatLng);

    }

    private void addCurrentLocationMarker() {
        MarkerOptions options = new MarkerOptions();

        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setStyle(IconGenerator.STYLE_PURPLE);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_my_location_marker));
        options.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());

        LatLng currentLatLng = new LatLng(0, 0);
        options.position(currentLatLng);
        Marker mapMarker = googleMap.addMarker(options);
        mapMarker.setTitle("My Current Location");
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng,
                17));

    }

    @Override
    protected void onPause() {
        super.onPause();
        //stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            //startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }

    @Override
    public void onMyTripDetailsFetched(TripModel tripModel) {
        Intent intent = new Intent(LocationActivity.this, TripReviewActivity.class);
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