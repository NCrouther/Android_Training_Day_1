package com.bignerdranch.android.networkingarchitecture.controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.android.networkingarchitecture.R;
import com.bignerdranch.android.networkingarchitecture.listener.VenueCheckInListener;
import com.bignerdranch.android.networkingarchitecture.listener.VenueGetHoursListener;
import com.bignerdranch.android.networkingarchitecture.model.HoursResponse;
import com.bignerdranch.android.networkingarchitecture.model.TimeFrame;
import com.bignerdranch.android.networkingarchitecture.model.TimeFrames;
import com.bignerdranch.android.networkingarchitecture.model.TimeRange;
import com.bignerdranch.android.networkingarchitecture.model.TokenStore;
import com.bignerdranch.android.networkingarchitecture.model.Venue;
import com.bignerdranch.android.networkingarchitecture.web.DataManager;

import java.util.List;
import java.util.Locale;

public class VenueDetailFragment extends Fragment implements VenueCheckInListener, VenueGetHoursListener {
    private static final String ARG_VENUE_ID = "VenueDetailFragment.VenueId";
    private static final String EXPIRED_DIALOG = "expired_dialog";

    private DataManager mDataManager;
    private String mVenueId;
    private Venue mVenue;
    private TextView mVenueNameTextView;
    private TextView mVenueAddressTextView;
    private TextView mVenueCheckinsTextView;
    private TextView mVenueUsersTextView;
    private TextView mVenueTipsTextView;
    private TextView mVenueHoursTextView;
    private Button mCheckInButton;
    private TokenStore mTokenStore;

    public static VenueDetailFragment newInstance(String venueId) {
        VenueDetailFragment fragment = new VenueDetailFragment();

        Bundle args = new Bundle();
        args.putString(ARG_VENUE_ID, venueId);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTokenStore = TokenStore.get(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_venue_detail, container, false);
        mVenueNameTextView = (TextView) view.findViewById(R.id.fragment_venue_detail_venue_name_text_view);
        mVenueAddressTextView = (TextView) view.findViewById(R.id.fragment_venue_detail_venue_address_text_view);
        mVenueCheckinsTextView = (TextView) view.findViewById(R.id.fragment_venue_detail_venue_number_checkins_text_view);
        mVenueUsersTextView = (TextView) view.findViewById(R.id.fragment_venue_detail_venue_number_users_text_view);
        mVenueTipsTextView = (TextView) view.findViewById(R.id.fragment_venue_detail_venue_number_tips_text_view);
        mVenueHoursTextView = (TextView) view.findViewById(R.id.fragment_venue_detail_venue_hours_text_view);
        mCheckInButton = (Button) view.findViewById(R.id.fragment_venue_detail_check_in_button);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mVenueId = getArguments().getString(ARG_VENUE_ID);
        mDataManager = DataManager.get(getActivity());
        mDataManager.addVenueCheckInListener(this);
        mDataManager.addGetHoursListener(this);
        mVenue = mDataManager.getVenue(mVenueId);

        mDataManager.fetchVenueHours(mVenueId);
    }

    @Override
    public void onResume() {
        super.onResume();
        mVenueNameTextView.setText(mVenue.getName());
        mVenueAddressTextView.setText(mVenue.getFormattedAddress());

        mVenueCheckinsTextView.setText(
                String.format(
                        Locale.getDefault(),
                        "Number Checkins: %d",
                        mVenue.getStats().getCheckinsCount()));
        mVenueUsersTextView.setText(
                String.format(
                        Locale.getDefault(),
                        "Number Users: %d",
                        mVenue.getStats().getUsersCount()));
        mVenueTipsTextView.setText(
                String.format(
                        Locale.getDefault(),
                        "Number Tips: %d",
                        mVenue.getStats().getTipCount()));
        mVenueHoursTextView.setVisibility(View.GONE);

        if (mTokenStore.getAccessToken() != null) {
            mCheckInButton.setVisibility(View.VISIBLE);
            mCheckInButton.setOnClickListener(mCheckInClickListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mDataManager.removeVenueCheckInListener(this);
        mDataManager.removeGetHoursListener(this);
    }

    private View.OnClickListener mCheckInClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mDataManager.checkInToVenue(mVenueId);
        }
    };

    @Override
    public void onVenueCheckInFinished() {
        Toast.makeText(getActivity(), R.string.successful_check_in_message,
                Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void onGetHoursComplete(HoursResponse hoursResponse) {
        String openHoursText = parseHours(hoursResponse.getOpenHours());
        if (openHoursText != null) {
            mVenueHoursTextView.setVisibility(View.VISIBLE);
            mVenueHoursTextView.setText(openHoursText);
        }
    }

    @Nullable
    private String parseHours(TimeFrames hours) {
        if (hours != null) {
            List<TimeFrame> timeFrames = hours.getTimeFrames();
            if (timeFrames != null && !timeFrames.isEmpty()) {
                StringBuilder hoursBuilder = new StringBuilder();
                for (TimeFrame timeFrame : timeFrames) {
                    for (int day : timeFrame.getDays()) {
                        hoursBuilder.append(
                                String.format(Locale.getDefault(), "Day %d:", day));
                        for (TimeRange range : timeFrame.getTimeRanges()) {
                            hoursBuilder.append(
                                    String.format(
                                            Locale.getDefault(),
                                            " %s-%s",
                                            range.getStart(),
                                            range.getEnd()));
                        }
                        hoursBuilder.append('\n');
                    }
                }
                return hoursBuilder.toString();
            }
        }
        return null;
    }

    @Override
    public void onTokenExpired() {
        mCheckInButton.setVisibility(View.GONE);
        ExpiredTokenDialogFragment dialogFragment = new ExpiredTokenDialogFragment();
        dialogFragment.show(getFragmentManager(), EXPIRED_DIALOG);
    }
}
