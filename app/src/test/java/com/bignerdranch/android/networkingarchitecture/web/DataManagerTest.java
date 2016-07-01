package com.bignerdranch.android.networkingarchitecture.web;

import com.bignerdranch.android.networkingarchitecture.BuildConfig;
import com.bignerdranch.android.networkingarchitecture.exception.UnauthorizedException;
import com.bignerdranch.android.networkingarchitecture.listener.VenueCheckInListener;
import com.bignerdranch.android.networkingarchitecture.listener.VenueSearchListener;
import com.bignerdranch.android.networkingarchitecture.model.TokenStore;
import com.bignerdranch.android.networkingarchitecture.model.Venue;
import com.bignerdranch.android.networkingarchitecture.model.VenueSearchResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import rx.Observable;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Ignore
@RunWith(RobolectricGradleTestRunner.class)
@Config(sdk = 21, constants = BuildConfig.class)
public class DataManagerTest {
    @Captor
    private ArgumentCaptor<Callback<VenueSearchResponse>> mSearchCaptor;
    private DataManager mDataManager;
    private static Retrofit mBasicRestAdapter = mock(Retrofit.class);
    private static Retrofit mAuthenticatedRestAdapter = mock(Retrofit.class);
    private static VenueInterface mVenueInterface = mock(VenueInterface.class);
    private static VenueSearchListener mVenueSearchListener = mock(VenueSearchListener.class);
    private static VenueCheckInListener mVenueCheckInListener = mock(VenueCheckInListener.class);


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mDataManager = DataManager.get(RuntimeEnvironment.application,
                mBasicRestAdapter, mAuthenticatedRestAdapter);

        when(mBasicRestAdapter.create(VenueInterface.class))
                .thenReturn(mVenueInterface);
        when(mAuthenticatedRestAdapter.create(VenueInterface.class))
                .thenReturn(mVenueInterface);
        mDataManager.addVenueSearchListener(mVenueSearchListener);
        mDataManager.addVenueCheckInListener(mVenueCheckInListener);
    }

    @After
    public void tearDown() {
        // clear DataManager state in between tests
        reset(mBasicRestAdapter, mAuthenticatedRestAdapter, mVenueInterface,
                mVenueSearchListener, mVenueCheckInListener);
        mDataManager.removeVenueSearchListener(mVenueSearchListener);
        mDataManager.removeVenueCheckInListener(mVenueCheckInListener);
    }

    @Test
    public void searchListenerTriggeredOnSuccessfulSearch() {
        mDataManager.fetchVenueSearchLatLong();
        verify(mVenueInterface).venueSearchByLatLong(anyString()).enqueue(mSearchCaptor.capture());
        VenueSearchResponse venueSearchResponse = mock(VenueSearchResponse.class);
        Response response = mock(Response.class);
        when(response.body()).thenReturn(venueSearchResponse);
        mSearchCaptor.getValue().onResponse(null, response);
        verify(mVenueSearchListener).onVenueSearchFinished();
    }

    @Test
    public void venueSearchListSavedOnSuccessfulSearch() {
        mDataManager.fetchVenueSearchLatLong();
        verify(mVenueInterface).venueSearchByLatLong(anyString()).enqueue(mSearchCaptor.capture());
        String firstVenueName = "Cool first venue";
        Venue firstVenue = mock(Venue.class);
        when(firstVenue.getName()).thenReturn(firstVenueName);

        String secondVenueName = "awesome second venue";
        Venue secondVenue = mock(Venue.class);
        when(secondVenue.getName()).thenReturn(secondVenueName);

        List<Venue> venueList = new ArrayList<>();
        venueList.add(firstVenue);
        venueList.add(secondVenue);

        VenueSearchResponse venueSearchResponse = mock(VenueSearchResponse.class);
        when(venueSearchResponse.getVenueList()).thenReturn(venueList);
        Response response = mock(Response.class);
        when(response.body()).thenReturn(venueSearchResponse);
        mSearchCaptor.getValue().onResponse(null, response);
        List<Venue> dataManagerVenueList = mDataManager.getVenueList();
        assertThat(dataManagerVenueList, is(equalTo(venueList)));
    }

    @Test
    public void checkInListenerTriggeredOnSuccessfulCheckIn() {
        Observable<Object> successObservable = Observable.just(new Object());
        when(mVenueInterface.venueCheckIn(anyString())).thenReturn(successObservable);
        String fakeVenueId = "fakeVenueId";
        mDataManager.checkInToVenue(fakeVenueId);
        verify(mVenueCheckInListener).onVenueCheckInFinished();
    }

    @Test()
    public void checkInListenerNotifiesTokenExpiredOnUnauthorizedException() {
        Observable<Object> unauthorizedObservable =
                Observable.error(new UnauthorizedException());
        when(mVenueInterface.venueCheckIn(anyString()))
                .thenReturn(unauthorizedObservable);
        String fakeVenueId = "fakeVenueId";
        mDataManager.checkInToVenue(fakeVenueId);
        verify(mVenueCheckInListener).onTokenExpired();
    }

    @Test()
    public void checkInListenerDoesNotNotifyTokenExpiredOnPlainException() {
        Observable<Object> runtimeObservable =
                Observable.error(new RuntimeException());
        when(mVenueInterface.venueCheckIn(anyString())).thenReturn(runtimeObservable);
        String fakeVenueId = "fakeVenueId";
        mDataManager.checkInToVenue(fakeVenueId);
        verify(mVenueCheckInListener, never()).onTokenExpired();
    }

    @Test()
    public void tokenClearedFromTokenStoreOnUnauthorizedException() {
        String testToken = "asdf1234";
        TokenStore tokenStore = TokenStore.get(RuntimeEnvironment.application);
        tokenStore.setAccessToken(testToken);
        assertThat(tokenStore.getAccessToken(), is(equalTo(testToken)));
        Observable<Object> unauthorizedObservable =
                Observable.error(new UnauthorizedException());
        when(mVenueInterface.venueCheckIn(anyString()))
                .thenReturn(unauthorizedObservable);
        String fakeVenueId = "fakeVenueId";
        mDataManager.checkInToVenue(fakeVenueId);
        assertThat(tokenStore.getAccessToken(), is(equalTo(null)));
    }
}
