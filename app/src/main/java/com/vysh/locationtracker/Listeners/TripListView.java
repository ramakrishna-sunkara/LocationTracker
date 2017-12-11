package com.vysh.locationtracker.Listeners;

import com.vysh.locationtracker.TripModel;

import java.util.List;

/**
 * Created by ramakrishna on 12/2/17.
 */

public interface TripListView {
    void onTripDetailsFetched(List<TripModel> tripModels);
    void onTripDetailsEmpty();
}
