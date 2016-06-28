package com.bignerdranch.android.networkingarchitecture.viewmodel;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.Nullable;
import android.view.View;

import com.bignerdranch.android.networkingarchitecture.BR;
import com.bignerdranch.android.networkingarchitecture.model.TimeFrame;
import com.bignerdranch.android.networkingarchitecture.model.TimeFrames;
import com.bignerdranch.android.networkingarchitecture.model.TimeRange;
import com.bignerdranch.android.networkingarchitecture.model.Venue;

import java.util.List;
import java.util.Locale;

public class VenueDetailViewModel extends BaseObservable {
    private final Venue mVenue;
    private String mHours;
    private int mCheckinVisibility;

    public VenueDetailViewModel(Venue venue) {
        this.mVenue = venue;
        showCheckinButton(false);
    }

    public String getVenueNameText() {
        return mVenue.getName();
    }

    public String getVenueAddressText() {
        return mVenue.getFormattedAddress();
    }

    public String getVenueNumberCheckinsText() {
        return String.format(
                Locale.getDefault(),
                "Number Checkins: %d",
                mVenue.getStats().getCheckinsCount());
    }

    public String getVenueNumberUsersText() {
        return String.format(
                Locale.getDefault(),
                "Number Users: %d",
                mVenue.getStats().getUsersCount());
    }

    public String getVenueNumberTipsText() {
        return String.format(
                Locale.getDefault(),
                "Number Tips: %d",
                mVenue.getStats().getTipCount());
    }

    @Bindable
    public int getHoursVisibility() {
        return mHours == null ? View.GONE : View.VISIBLE;
    }

    @Bindable
    public String getHoursText() {
        return mHours;
    }

    public void updateHours(TimeFrames openHours) {
        mHours = parseHours(openHours);
        notifyPropertyChanged(BR.hoursText);
        notifyPropertyChanged(BR.hoursVisibility);
    }

    @Nullable
    private static String parseHours(TimeFrames hours) {
        if (hours != null) {
            List<TimeFrame> timeFrames = hours.getTimeFrames();
            if (timeFrames != null && !timeFrames.isEmpty()) {
                StringBuilder hoursBuilder = new StringBuilder();
                for (TimeFrame timeFrame : timeFrames) {
                    for (int day : timeFrame.getDays()) {
                        hoursBuilder.append(
                                String.format(Locale.getDefault(), "Day %d:", day));
                        for (TimeRange range : timeFrame.getTimeRanges()) {
                            hoursBuilder.append(
                                    String.format(
                                            Locale.getDefault(),
                                            " %s-%s",
                                            range.getStart(),
                                            range.getEnd()));
                        }
                        hoursBuilder.append('\n');
                    }
                }
                return hoursBuilder.toString();
            }
        }
        return null;
    }

    @Bindable
    public int getCheckinVisibility() {
        return mCheckinVisibility;
    }

    public void showCheckinButton(boolean show) {
        mCheckinVisibility = show ? View.VISIBLE : View.GONE;
        notifyPropertyChanged(BR.checkinVisibility);
    }
}
