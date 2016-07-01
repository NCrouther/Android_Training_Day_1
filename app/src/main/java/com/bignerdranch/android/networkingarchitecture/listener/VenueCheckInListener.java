package com.bignerdranch.android.networkingarchitecture.listener;

public interface VenueCheckInListener extends AuthenticationListener {
    void onVenueCheckInFinished();

    void onVenueCheckInRetry();

    void onVenueCheckInTooSoon();
}
