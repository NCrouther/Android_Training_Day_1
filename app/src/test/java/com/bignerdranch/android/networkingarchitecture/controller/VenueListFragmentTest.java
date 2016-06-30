package com.bignerdranch.android.networkingarchitecture.controller;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bignerdranch.android.networkingarchitecture.BuildConfig;
import com.bignerdranch.android.networkingarchitecture.R;
import com.bignerdranch.android.networkingarchitecture.SynchronousExecutor;
import com.bignerdranch.android.networkingarchitecture.model.VenueSearchResponse;
import com.bignerdranch.android.networkingarchitecture.web.DataManager;
import com.bignerdranch.android.networkingarchitecture.web.VenueListDeserializer;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = 21, constants = BuildConfig.class)
public class VenueListFragmentTest {
    @Rule
    public WireMockRule mWireMockRule = new WireMockRule(1111);
    private String mEndpoint = "http://localhost:1111/";
    private DataManager mDataManager;
    private VenueListActivity mVenueListActivity;
    private VenueListFragment mVenueListFragment;

    @Before
    public void setUp() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(VenueSearchResponse.class,
                        new VenueListDeserializer())
                .create();
        SynchronousExecutor executor = new SynchronousExecutor();
        OkHttpClient client = new OkHttpClient.Builder()
                .dispatcher(new Dispatcher(new SynchronousExecutor())).build();
        Retrofit basicRestAdapter = new Retrofit.Builder()
                .baseUrl(mEndpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
        mDataManager = DataManager.get(
                RuntimeEnvironment.application, basicRestAdapter, null);

        stubFor(get(urlMatching("/venues/search.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBodyFile("search.json")));

        mVenueListActivity = Robolectric.buildActivity(VenueListActivity.class)
                .create().start().resume().get();
        mVenueListFragment = (VenueListFragment) mVenueListActivity
                .getSupportFragmentManager()
                .findFragmentById(R.id.fragmentContainer);
    }

    @Test
    public void activityListsVenuesReturnedFromSearch() {
        assertThat(mVenueListFragment, is(notNullValue()));
        RecyclerView venueRecyclerView = (RecyclerView) mVenueListFragment.getView()
                .findViewById(R.id.venueListRecyclerView);
        assertThat(venueRecyclerView, is(notNullValue()));
        assertThat(venueRecyclerView.getAdapter().getItemCount(), is(2));

        venueRecyclerView.measure(0, 0);
        venueRecyclerView.layout(0, 0, 100, 1000);

        String bnrTitle = "BNR Intergalactic Headquarters";
        String rndTitle = "Ration and Dram";
        View firstVenueView = venueRecyclerView.getChildAt(0);
        TextView venueTitleTextView = (TextView) firstVenueView
                .findViewById(R.id.view_venue_list_VenueTitleTextView);
        assertThat(venueTitleTextView.getText(), is(equalTo(bnrTitle)));
        View secondVenueView = venueRecyclerView.getChildAt(1);
        TextView venueTitleTextView2 = (TextView) secondVenueView
                .findViewById(R.id.view_venue_list_VenueTitleTextView);
        assertThat(venueTitleTextView2.getText(), is(equalTo(rndTitle)));

    }

}

