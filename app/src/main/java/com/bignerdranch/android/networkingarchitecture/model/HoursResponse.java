package com.bignerdranch.android.networkingarchitecture.model;

import com.google.gson.annotations.SerializedName;

public class HoursResponse {
    @SerializedName("hours")
    private TimeFrame mOpenHours;

    @SerializedName("popular")
    private TimeFrame mPopularHours;

    public TimeFrame getOpenHours() {
        return mOpenHours;
    }

    public TimeFrame getPopularHours() {
        return mPopularHours;
    }
}
