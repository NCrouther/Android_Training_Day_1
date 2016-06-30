package com.bignerdranch.android.networkingarchitecture.controller;

import android.app.AlertDialog;
import android.content.Intent;
import android.widget.Button;

import com.bignerdranch.android.networkingarchitecture.BuildConfig;
import com.bignerdranch.android.networkingarchitecture.R;
import com.bignerdranch.android.networkingarchitecture.SynchronousExecutor;
import com.bignerdranch.android.networkingarchitecture.model.TokenStore;
import com.bignerdranch.android.networkingarchitecture.model.VenueSearchResponse;
import com.bignerdranch.android.networkingarchitecture.web.DataManager;
import com.bignerdranch.android.networkingarchitecture.web.RetrofitErrorHandler;
import com.bignerdranch.android.networkingarchitecture.web.VenueListDeserializer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowToast;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.robolectric.Shadows.shadowOf;

@Ignore
@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = 21, constants = BuildConfig.class)
public class VenueDetailFragmentTest {
    @Rule
    public WireMockRule mWireMockRule = new WireMockRule(1111);
    private String mEndpoint = "http://localhost:1111/";
    private DataManager mDataManager;
    private VenueDetailActivity mVenueDetailActivity;
    private VenueDetailFragment mVenueDetailFragment;

    @Before
    public void setUp() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(VenueSearchResponse.class,
                        new VenueListDeserializer())
                .create();

        OkHttpClient client = new OkHttpClient.Builder()
                .dispatcher(new Dispatcher(new SynchronousExecutor())).build();
        Retrofit basicRestAdapter = new Retrofit.Builder()
                .baseUrl(mEndpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build();

        OkHttpClient authenticatedClient = new OkHttpClient.Builder()
                .addInterceptor(new RetrofitErrorHandler())
                .dispatcher(new Dispatcher(new SynchronousExecutor())).build();
        Retrofit authenticatedRestAdapter = new Retrofit.Builder()
                .baseUrl(mEndpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(authenticatedClient)
                .build();
        mDataManager = DataManager.get(RuntimeEnvironment.application,
                basicRestAdapter, authenticatedRestAdapter);

        stubFor(get(urlMatching("/venues/search.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("search.json")));
        mDataManager.fetchVenueSearch();

        TokenStore tokenStore = TokenStore.get(RuntimeEnvironment.application);
        tokenStore.setAccessToken("bogus token for testing");
    }

    @Test
    public void toastShownOnSuccessfulCheckIn() {
        stubFor(post(urlMatching("/checkins/add.*"))
                .willReturn(aResponse()
                        .withStatus(200)));
        String bnrVenueId = "527c1d4f11d20f41ba39fc01";
        Intent detailIntent = VenueDetailActivity
                .newIntent(RuntimeEnvironment.application, bnrVenueId);
        mVenueDetailActivity = Robolectric.buildActivity(VenueDetailActivity.class)
                .withIntent(detailIntent)
                .create().start().resume().get();
        mVenueDetailFragment = (VenueDetailFragment) mVenueDetailActivity
                .getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        Button checkInButton = (Button) mVenueDetailFragment.getView()
                .findViewById(R.id.fragment_venue_detail_check_in_button);
        ShadowLooper.idleMainLooper();
        checkInButton.performClick();
        ShadowLooper.idleMainLooper();
        String expectedToastText = mVenueDetailActivity
                .getString(R.string.successful_check_in_message);
        assertThat(ShadowToast.getTextOfLatestToast(),
                is(equalTo(expectedToastText)));
    }

    @Test
    public void errorDialogShownOnUnauthorizedException() {
        stubFor(post(urlMatching("/checkins/add.*"))
                .willReturn(aResponse()
                        .withStatus(401)));
        String bnrVenueId = "527c1d4f11d20f41ba39fc01";
        Intent detailIntent = VenueDetailActivity
                .newIntent(RuntimeEnvironment.application, bnrVenueId);
        mVenueDetailActivity = Robolectric
                .buildActivity(VenueDetailActivity.class)
                .withIntent(detailIntent)
                .create().start().resume().get();
        mVenueDetailFragment = (VenueDetailFragment) mVenueDetailActivity
                .getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        Button checkInButton = (Button) mVenueDetailFragment.getView()
                .findViewById(R.id.fragment_venue_detail_check_in_button);
        ShadowLooper.idleMainLooper();
        checkInButton.performClick();
        ShadowLooper.idleMainLooper();
        AlertDialog errorDialog = ShadowAlertDialog.getLatestAlertDialog();
        assertThat(errorDialog, is(notNullValue()));
        ShadowAlertDialog alertDialog = shadowOf(errorDialog);
        String expectedDialogTitle = mVenueDetailActivity
                .getString(R.string.expired_token_dialog_title);
        String expectedDialogMessage = mVenueDetailActivity
                .getString(R.string.expired_token_dialog_message);
        assertThat(alertDialog.getTitle(), is(equalTo(expectedDialogTitle)));
        assertThat(alertDialog.getMessage(), is(equalTo(expectedDialogMessage)));
    }

    @Test
    public void errorDialogNotShownOnDifferentException() {
        stubFor(post(urlMatching("/checkins/add.*"))
                .willReturn(aResponse()
                        .withStatus(500)));
        String bnrVenueId = "527c1d4f11d20f41ba39fc01";
        Intent detailIntent = VenueDetailActivity
                .newIntent(RuntimeEnvironment.application, bnrVenueId);
        mVenueDetailActivity = Robolectric.buildActivity(VenueDetailActivity.class)
                .withIntent(detailIntent)
                .create().start().resume().get();
        mVenueDetailFragment = (VenueDetailFragment) mVenueDetailActivity
                .getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        Button checkInButton = (Button) mVenueDetailFragment.getView()
                .findViewById(R.id.fragment_venue_detail_check_in_button);
        ShadowLooper.idleMainLooper();
        checkInButton.performClick();
        ShadowLooper.idleMainLooper();
        AlertDialog errorDialog = ShadowAlertDialog.getLatestAlertDialog();
        assertThat(errorDialog, is(nullValue()));
    }
}
