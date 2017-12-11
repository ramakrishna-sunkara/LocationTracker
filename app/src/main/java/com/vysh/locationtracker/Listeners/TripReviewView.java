package com.vysh.locationtracker.Listeners;

import com.vysh.locationtracker.TripModel;

/**
 * Created by ramakrishna on 12/2/17.
 */

public interface TripReviewView {
    void onMyTripDetailsFetched(TripModel tripModel);

    void onMyTripDetailsEmpty(int messageId);
}
