package com.bignerdranch.android.networkingarchitecture.model;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;

public class Icon {
    @SerializedName("prefix")
    private String mPrefix;
    @SerializedName("suffix")
    private String mSuffix;

    public String getPath(int size) {
        return String.format(Locale.getDefault(), "%s%d%s", mPrefix, size, mSuffix);
    }
}
