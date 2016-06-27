package com.bignerdranch.android.networkingarchitecture.model;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Venue {
    @SerializedName("id")
    private String mId;
    @SerializedName("name")
    private String mName;
    @SerializedName("verified")
    private boolean mVerified;
    @SerializedName("location")
    private Location mLocation;
    @SerializedName("categories")
    private List<Category> mCategoryList;
    @SerializedName("stats")
    private Stats mStats;

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getFormattedAddress() {
        return mLocation.getFormattedAddress();
    }

    @Nullable
    public String getPrimaryCategoryIconPath(int size) {
        if (!mCategoryList.isEmpty()) {
            return mCategoryList.get(0).getIcon().getPath(size);
        } else {
            return null;
        }
    }

    public Stats getStats() {
        return mStats;
    }
}
