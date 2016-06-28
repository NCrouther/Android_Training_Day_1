package com.bignerdranch.android.networkingarchitecture.model;

import com.google.gson.annotations.SerializedName;

public class HoursResponse {
    @SerializedName("hours")
    private TimeFrames mOpenHours;

    @SerializedName("popular")
    private TimeFrames mPopularHours;

    public TimeFrames getOpenHours() {
        return mOpenHours;
    }

    public TimeFrames getPopularHours() {
        return mPopularHours;
    }
}
