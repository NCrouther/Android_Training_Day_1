package com.bignerdranch.android.networkingarchitecture.listener;

import com.bignerdranch.android.networkingarchitecture.model.HoursResponse;

public interface VenueGetHoursListener {
    void onGetHoursComplete(HoursResponse hoursResponse);
}
