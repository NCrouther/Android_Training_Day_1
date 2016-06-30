package com.bignerdranch.android.networkingarchitecture.controller;

import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.core.deps.guava.io.ByteStreams;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.bignerdranch.android.networkingarchitecture.model.HoursResponse;
import com.bignerdranch.android.networkingarchitecture.model.TokenStore;
import com.bignerdranch.android.networkingarchitecture.model.VenueSearchResponse;
import com.bignerdranch.android.networkingarchitecture.web.DataManager;
import com.bignerdranch.android.networkingarchitecture.web.HoursDeserializer;
import com.bignerdranch.android.networkingarchitecture.web.RetrofitErrorHandler;
import com.bignerdranch.android.networkingarchitecture.web.VenueListDeserializer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

@RunWith(AndroidJUnit4.class)
public class VenueListActivityTest {
    @Rule
    public WireMockRule mWireMockRule = new WireMockRule(1111);

    @Rule
    public ActivityTestRule<VenueListActivity> mActivityRule
            = new ActivityTestRule<VenueListActivity>(VenueListActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            stubFor(get(urlMatching("/venues/search.*"))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withBody(readAssetText("search.json"))));

            configureDataManager();
        }
    };

    @NonNull
    private String readAssetText(String fileName) {
        try {
            InputStream stream = InstrumentationRegistry.getContext().getAssets().open(fileName);
            byte[] bytes = ByteStreams.toByteArray(stream);
            return new String(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public void setUp() {
        TokenStore tokenStore = TokenStore.get(InstrumentationRegistry.getTargetContext());
        tokenStore.setAccessToken("bogus token for testing");
    }

    private void configureDataManager() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(VenueSearchResponse.class,
                        new VenueListDeserializer())
                .registerTypeAdapter(HoursResponse.class,
                        new HoursDeserializer())
                .create();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor).build();
        String wiremockEndpoint = "http://localhost:1111/";
        Retrofit basicRestAdapter = new Retrofit.Builder()
                .baseUrl(wiremockEndpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build();

        OkHttpClient authenticatedClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(new RetrofitErrorHandler()).build();
        Retrofit authenticatedRestAdapter = new Retrofit.Builder()
                .baseUrl(wiremockEndpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(authenticatedClient)
                .build();
        DataManager.get(InstrumentationRegistry.getTargetContext(),
                basicRestAdapter, authenticatedRestAdapter);
    }

    @Test
    public void activityListsVenuesReturnedFromSearch() {
        String bnrTitle = "BNR Intergalactic Headquarters";
        String rndTitle = "Ration and Dram";

        onView(withContentDescription(bnrTitle)).check(matches(isDisplayed()));
        onView(withContentDescription(rndTitle)).check(matches(isDisplayed()));
    }
}

