package com.bignerdranch.android.networkingarchitecture.web;

import com.bignerdranch.android.networkingarchitecture.model.HoursResponse;
import com.bignerdranch.android.networkingarchitecture.model.VenueSearchResponse;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

public interface VenueInterface {
    @GET("/venues/search")
    void venueSearch(@Query("ll") String latLngString, Callback<VenueSearchResponse> callback);

    @GET("/venues/{venueId}/hours")
    void venueHours(@Path("venueId") String venueId, Callback<HoursResponse> callback);

    @FormUrlEncoded
    @POST("/checkins/add")
    Observable<Object> venueCheckIn(@Field("venueId") String venueId);
}
