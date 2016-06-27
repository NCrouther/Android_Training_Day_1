package com.bignerdranch.android.networkingarchitecture.model;

import com.google.gson.annotations.SerializedName;

public class Stats {
    @SerializedName("checkinsCount")
    private int mCheckinsCount;
    @SerializedName("usersCount")
    private int mUsersCount;
    @SerializedName("tipCount")
    private int mTipCount;

    public int getCheckinsCount() {
        return mCheckinsCount;
    }

    public int getUsersCount() {
        return mUsersCount;
    }

    public int getTipCount() {
        return mTipCount;
    }
}
