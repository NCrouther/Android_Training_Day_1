package com.bignerdranch.android.networkingarchitecture.model;

import com.google.gson.annotations.SerializedName;

public class TimeRange {
    @SerializedName("start")
    private String mStart;
    @SerializedName("end")
    private String mEnd;

    public String getStart() {
        return mStart;
    }

    public String getEnd() {
        return mEnd;
    }
}
