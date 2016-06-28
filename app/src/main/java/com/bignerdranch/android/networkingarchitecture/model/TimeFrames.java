package com.bignerdranch.android.networkingarchitecture.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TimeFrames {
    @SerializedName("timeframes")
    private List<TimeFrame> mTimeFrames;

    public List<TimeFrame> getTimeFrames() {
        return mTimeFrames;
    }
}
