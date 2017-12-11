package com.vysh.locationtracker;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;
import com.vysh.locationtracker.Listeners.TripReviewView;

public class TripReviewActivity extends AppCompatActivity implements
        LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, TripReviewView {

    private static final String TAG = "LocationActivity";
    private static final long INTERVAL = 1000 * 30 * 1; //1 minute
    private static final long FASTEST_INTERVAL = 1000 * 30 * 1; // 1 minute
    public static final String EXTRA_TRIP_ID = "TripId";
    Button btnFusedLocation;
    TextView tvLocation;
    LocationRequest mLocationRequest;
    //GoogleApiClient mGoogleApiClient;
    GoogleMap googleMap;
    boolean isTripStarted = false;
    int tripId;
    //int endPointPosition;

    TripReviewPresenter tripReviewPresenter;
    Toolbar toolbar;

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

        tripId = getIntent().getIntExtra(EXTRA_TRIP_ID, 1);

        createLocationRequest();
        /*mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();*/

        setContentView(R.layout.activity_trip_review);

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        googleMap = fm.getMap();
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        tripReviewPresenter = new TripReviewPresenter(getApplicationContext(), this);
        tripReviewPresenter.showMyTripDirections(tripId);

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        // mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop fired ..............");
        //  mGoogleApiClient.disconnect();
        // Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
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
        // Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
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
        // PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
        //       mGoogleApiClient, mLocationRequest, this);
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
        Toast.makeText(this, "Location Updated...", Toast.LENGTH_SHORT).show();
    }

    private void addMarker(LatLng latLng, String message, int position) {
        MarkerOptions options = new MarkerOptions();
        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setStyle(IconGenerator.STYLE_PURPLE);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_start));
        options.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
        options.position(latLng);
        Marker mapMarker = googleMap.addMarker(options);
        mapMarker.setTitle(message);
        if (position == 0) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,
                    16));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        // LocationServices.FusedLocationApi.removeLocationUpdates(
        //        mGoogleApiClient, this);
        Log.d(TAG, "Location update stopped .......................");
    }

    @Override
    public void onResume() {
        super.onResume();
        //if (mGoogleApiClient.isConnected()) {
        //startLocationUpdates();
        Log.d(TAG, "Location update resumed .....................");
        // }
    }

    @Override
    public void onMyTripDetailsFetched(TripModel tripModel) {

        PolylineOptions polyLineOptions = new PolylineOptions();

        if (tripModel.getTripLocationPoints() != null && tripModel.getTripLocationPoints().size() > 1) {
            addMarker(tripModel.getTripLocationPoints().get(0), "Source", 0);
            addMarker(tripModel.getTripLocationPoints().get(tripModel.getTripLocationPoints().size() - 1), "Destination", tripModel.getTripLocationPoints().size() - 1);

            polyLineOptions.addAll(tripModel.getTripLocationPoints());
            polyLineOptions.width(12);
            polyLineOptions.color(Color.BLUE);
        }

        if (polyLineOptions != null) {
            googleMap.addPolyline(polyLineOptions);
        }
    }

    @Override
    public void onMyTripDetailsEmpty(int messageId) {
        Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
    }
}