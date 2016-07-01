package com.bignerdranch.android.networkingarchitecture.web;

import com.bignerdranch.android.networkingarchitecture.model.HoursResponse;
import com.bignerdranch.android.networkingarchitecture.model.VenueSearchResponse;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

public interface VenueInterface {
    @GET("venues/search")
    Call<VenueSearchResponse> venueSearchByLatLong(@Query("ll") String latLngString);

    @GET("venues/search")
    Call<VenueSearchResponse> venueSearchByLocation(@Query("near") String locationString);

    @GET("venues/{venueId}/hours")
    Call<HoursResponse> venueHours(@Path("venueId") String venueId);

    @FormUrlEncoded
    @POST("checkins/add")
    Observable<Object> venueCheckIn(@Field("venueId") String venueId);
}
