package com.bignerdranch.android.networkingarchitecture.web;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.bignerdranch.android.networkingarchitecture.exception.UnauthorizedException;
import com.bignerdranch.android.networkingarchitecture.listener.VenueCheckInListener;
import com.bignerdranch.android.networkingarchitecture.listener.VenueGetHoursListener;
import com.bignerdranch.android.networkingarchitecture.listener.VenueSearchListener;
import com.bignerdranch.android.networkingarchitecture.model.HoursResponse;
import com.bignerdranch.android.networkingarchitecture.model.TokenStore;
import com.bignerdranch.android.networkingarchitecture.model.Venue;
import com.bignerdranch.android.networkingarchitecture.model.VenueSearchResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class DataManager {
    private static final String TAG = "DataManager";
    private static final String FOURSQUARE_ENDPOINT
            = "https://api.foursquare.com/v2/";
    private static final String OAUTH_ENDPOINT
            = "https://foursquare.com/oauth2/authenticate";
    public static final String OAUTH_REDIRECT_URI
            = "http://bignerdranch.com";
    private static final String CLIENT_ID
            = "5IRZOLAX3WONUURF3NACQ40SLB50QASH14A5O0VY5F4TNQJF";
    private static final String CLIENT_SECRET
            = "HTGCWKVPPMSFUBLNPWXF4EI4PQQADDLTSGLK5DFMX1LC3VZP";
    private static final String FOURSQUARE_VERSION = "20150406";
    private static final String FOURSQUARE_MODE = "foursquare";
    private static final String SWARM_MODE = "swarm";
    private static final String TEST_LAT_LNG = "33.759,-84.332";
    private static final String TEST_LOCATION = "San Francisco, CA";
    private static final String LAST_CHECKIN_TIMES = "lastCheckinTimes";
    private List<Venue> mVenueList;
    private List<VenueSearchListener> mSearchListenerList;
    private List<VenueCheckInListener> mCheckInListenerList;
    private List<VenueGetHoursListener> mGetHoursListenerList;

    private static DataManager sDataManager;
    private Context mContext;
    private static TokenStore sTokenStore;
    private Retrofit mBasicRestAdapter;
    private Retrofit mAuthenticatedRestAdapter;

    public static DataManager get(Context context) {
        if (sDataManager == null) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(VenueSearchResponse.class,
                            new VenueListDeserializer())
                    .registerTypeAdapter(HoursResponse.class,
                            new HoursDeserializer())
                    .create();

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .addInterceptor(sRequestInterceptor).build();
            Retrofit basicRestAdapter = new Retrofit.Builder()
                    .baseUrl(FOURSQUARE_ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(client)
                    .build();

            OkHttpClient authenticatedClient = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .addInterceptor(sAuthenticatedRequestInterceptor)
                    .addInterceptor(new RetrofitErrorHandler()).build();
            Retrofit authenticatedRestAdapter = new Retrofit.Builder()
                    .baseUrl(FOURSQUARE_ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .client(authenticatedClient)
                    .build();
            sDataManager = new DataManager(context, basicRestAdapter, authenticatedRestAdapter);
        }
        return sDataManager;
    }

    @VisibleForTesting
    public static DataManager get(Context context, Retrofit basicRestAdapter,
                                  Retrofit authenticatedRestAdapter) {
        sDataManager = new DataManager(context, basicRestAdapter,
                authenticatedRestAdapter);
        return sDataManager;
    }

    private DataManager(
            Context context,
            Retrofit basicRestAdapter,
            Retrofit authenticatedRestAdapter) {
        mContext = context.getApplicationContext();
        sTokenStore = TokenStore.get(mContext);
        mBasicRestAdapter = basicRestAdapter;
        mAuthenticatedRestAdapter = authenticatedRestAdapter;
        mSearchListenerList = new ArrayList<>();
        mCheckInListenerList = new ArrayList<>();
        mGetHoursListenerList = new ArrayList<>();
    }

    private Callback<VenueSearchResponse> venueSearchResponseCallback = new Callback<VenueSearchResponse>() {
        @Override
        public void onResponse(
                Call<VenueSearchResponse> call,
                Response<VenueSearchResponse> response) {
            if (response.isSuccessful()) {
                mVenueList = response.body().getVenueList();
                notifySearchListeners();
            } else {
                Log.e(TAG, String.format(Locale.getDefault(),
                        "Failed to fetch venue search: code %d", response.code()));
            }
        }

        @Override
        public void onFailure(Call<VenueSearchResponse> call, Throwable t) {
            Log.e(TAG, "Failed to fetch venue search", t);
        }
    };

    public void fetchVenueSearchLatLong() {
        VenueInterface venueInterface = mBasicRestAdapter.create(VenueInterface.class);
        venueInterface.venueSearchByLatLong(TEST_LAT_LNG).enqueue(venueSearchResponseCallback);
    }

    public void fetchVenueSearchLocation() {
        VenueInterface venueInterface = mBasicRestAdapter.create(VenueInterface.class);
        venueInterface.venueSearchByLocation(TEST_LOCATION).enqueue(venueSearchResponseCallback);
    }

    public void checkInToVenue(String venueId) {
        SharedPreferences sp = mContext.getSharedPreferences(LAST_CHECKIN_TIMES, 0);
        long lastCheckinTicks = sp.getLong(venueId, 0);
        long currentTimeTicks = new Date().getTime();
        if (currentTimeTicks > lastCheckinTicks + TimeUnit.DAYS.toMillis(1)) {
            VenueInterface venueInterface =
                    mAuthenticatedRestAdapter.create(VenueInterface.class);
            venueInterface.venueCheckIn(venueId)
                    .doOnCompleted(() -> sp.edit().putLong(venueId, currentTimeTicks).apply())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            result -> notifyCheckInListeners(),
                            this::handleCheckInException
                    );
        } else {
            notifyCheckInListenersTooSoon();
        }
    }

    private void handleCheckInException(Throwable error) {
        if (error instanceof UnauthorizedException) {
            sTokenStore.setAccessToken(null);
            notifyCheckInListenersTokenExpired();
        } else {
            notifyCheckInListenersRetry();
        }
    }

    public void fetchVenueHours(String venueId) {
        VenueInterface venueInterface = mBasicRestAdapter.create(VenueInterface.class);
        venueInterface.venueHours(venueId).enqueue(new Callback<HoursResponse>() {
            @Override
            public void onResponse(Call<HoursResponse> call, Response<HoursResponse> response) {
                if (response.isSuccessful()) {
                    notifyGetHoursListeners(response.body());
                } else {
                    Log.e(TAG, String.format(Locale.getDefault(),
                            "Failed to get venue hours: code %d", response.code()));
                }
            }

            @Override
            public void onFailure(Call<HoursResponse> call, Throwable t) {
                Log.e(TAG, "Failed to get venue hours", t);
            }
        });
    }

    public List<Venue> getVenueList() {
        return mVenueList;
    }

    public Venue getVenue(String venueId) {
        for (Venue venue : mVenueList) {
            if (venue.getId().equals(venueId)) {
                return venue;
            }
        }
        return null;
    }

    public String getAuthenticationUrl() {
        return Uri.parse(OAUTH_ENDPOINT).buildUpon()
                .appendQueryParameter("client_id", CLIENT_ID)
                .appendQueryParameter("response_type", "token")
                .appendQueryParameter("redirect_uri", OAUTH_REDIRECT_URI)
                .build()
                .toString();
    }

    public void addVenueSearchListener(VenueSearchListener listener) {
        mSearchListenerList.add(listener);
    }

    public void removeVenueSearchListener(VenueSearchListener listener) {
        mSearchListenerList.remove(listener);
    }

    private void notifySearchListeners() {
        for (VenueSearchListener listener : mSearchListenerList) {
            listener.onVenueSearchFinished();
        }
    }

    public void addVenueCheckInListener(VenueCheckInListener listener) {
        mCheckInListenerList.add(listener);
    }

    public void removeVenueCheckInListener(VenueCheckInListener listener) {
        mCheckInListenerList.remove(listener);
    }

    private void notifyCheckInListeners() {
        for (VenueCheckInListener listener : mCheckInListenerList) {
            listener.onVenueCheckInFinished();
        }
    }

    public void addGetHoursListener(VenueGetHoursListener listener) {
        mGetHoursListenerList.add(listener);
    }

    public void removeGetHoursListener(VenueGetHoursListener listener) {
        mGetHoursListenerList.remove(listener);
    }

    private void notifyGetHoursListeners(HoursResponse hoursResponse) {
        for (VenueGetHoursListener listener : mGetHoursListenerList) {
            listener.onGetHoursComplete(hoursResponse);
        }
    }

    private void notifyCheckInListenersTokenExpired() {
        for (VenueCheckInListener listener : mCheckInListenerList) {
            listener.onTokenExpired();
        }
    }

    private void notifyCheckInListenersRetry() {
        for (VenueCheckInListener listener : mCheckInListenerList) {
            listener.onVenueCheckInRetry();
        }
    }

    private void notifyCheckInListenersTooSoon() {
        for (VenueCheckInListener listener : mCheckInListenerList) {
            listener.onVenueCheckInTooSoon();
        }
    }

    private static Interceptor sRequestInterceptor = chain -> chain.proceed(
            chain.request().newBuilder().url(
                    chain.request().url().newBuilder()
                            .addQueryParameter("client_id", CLIENT_ID)
                            .addQueryParameter("client_secret", CLIENT_SECRET)
                            .addQueryParameter("v", FOURSQUARE_VERSION)
                            .addQueryParameter("m", FOURSQUARE_MODE).build()).build());

    private static Interceptor sAuthenticatedRequestInterceptor = chain -> chain.proceed(
            chain.request().newBuilder().url(
                    chain.request().url().newBuilder()
                            .addQueryParameter("oauth_token", sTokenStore.getAccessToken())
                            .addQueryParameter("v", FOURSQUARE_VERSION)
                            .addQueryParameter("m", SWARM_MODE).build()).build());
}
