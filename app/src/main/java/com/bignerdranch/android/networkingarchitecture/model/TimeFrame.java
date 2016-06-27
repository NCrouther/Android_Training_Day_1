package com.bignerdranch.android.networkingarchitecture.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TimeFrame {
    @SerializedName("days")
    private List<Integer> mDays;
    @SerializedName("open")
    private List<TimeRange> mTimeRanges;

    public List<Integer> getDays() {
        return mDays;
    }

    public List<TimeRange> getTimeRanges() {
        return mTimeRanges;
    }
}
