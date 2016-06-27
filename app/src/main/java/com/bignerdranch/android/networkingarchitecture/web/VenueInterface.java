package com.bignerdranch.android.networkingarchitecture.web;

import com.bignerdranch.android.networkingarchitecture.model.VenueSearchResponse;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

public interface VenueInterface {
    @GET("/venues/search")
    void venueSearch(@Query("ll") String latLngString, Callback<VenueSearchResponse> callback);

    @FormUrlEncoded
    @POST("/checkins/add")
    void venueCheckIn(@Field("venueId") String venueId, Callback<Object> callback);
}
