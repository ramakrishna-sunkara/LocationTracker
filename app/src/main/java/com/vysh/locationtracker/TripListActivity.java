package com.vysh.locationtracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.vysh.locationtracker.Adapters.TripListAdapter;
import com.vysh.locationtracker.Listeners.TripListView;

import java.util.List;

/**
 * Created by ramakrishna on 12/2/17.
 */

public class TripListActivity extends AppCompatActivity implements TripListView, TripListAdapter.TripClickListener {

    RecyclerView rvTripList;
    TripListAdapter tripListAdapter;
    TextView txtNoTrips;
    TripListPresenter tripListPresenter;
    android.support.v7.widget.Toolbar toolbar;
    List<TripModel> mTripModels;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_list);
        rvTripList = (RecyclerView) findViewById(R.id.rvTripList);
        txtNoTrips = (TextView) findViewById(R.id.txtNoTrips);

        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvTripList.setLayoutManager(linearLayoutManager);
        rvTripList.setHasFixedSize(true);

        tripListPresenter = new TripListPresenter(getApplicationContext(), this);
        tripListPresenter.showAllTrips();
    }

    @Override
    public void onTripDetailsFetched(List<TripModel> tripModels) {
        mTripModels = tripModels;
        tripListAdapter = new TripListAdapter(mTripModels);
        rvTripList.setAdapter(tripListAdapter);
        tripListAdapter.setTripClickListener(this);
    }

    @Override
    public void onTripDetailsEmpty() {
        rvTripList.setVisibility(View.GONE);
        txtNoTrips.setVisibility(View.VISIBLE);
    }

    @Override
    public void onTripItemClick(TripModel tripModel) {
        if (tripModel.getTripLocationPoints() != null && tripModel.getTripLocationPoints().size() > 1) {
            Intent intent = new Intent(TripListActivity.this, TripReviewActivity.class);
            intent.putExtra(TripReviewActivity.EXTRA_TRIP_ID, tripModel.getTripId());
            startActivity(intent);
        }else {
            Toast.makeText(this, getString(R.string.msg_invalid_trip), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void deleteTripItem(TripModel tripModel) {
        tripListPresenter.deleteTripId(tripModel.getTripId());
        mTripModels.remove(tripModel);
        tripListAdapter.notifyDataSetChanged();
    }
}
