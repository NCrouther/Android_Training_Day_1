package com.bignerdranch.android.networkingarchitecture.controller;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bignerdranch.android.networkingarchitecture.R;
import com.bignerdranch.android.networkingarchitecture.databinding.FragmentVenueDetailBinding;
import com.bignerdranch.android.networkingarchitecture.listener.VenueCheckInListener;
import com.bignerdranch.android.networkingarchitecture.listener.VenueGetHoursListener;
import com.bignerdranch.android.networkingarchitecture.model.HoursResponse;
import com.bignerdranch.android.networkingarchitecture.model.TokenStore;
import com.bignerdranch.android.networkingarchitecture.viewmodel.VenueDetailViewModel;
import com.bignerdranch.android.networkingarchitecture.web.DataManager;

public class VenueDetailFragment extends Fragment implements VenueCheckInListener, VenueGetHoursListener {
    private static final String ARG_VENUE_ID = "VenueDetailFragment.VenueId";
    private static final String EXPIRED_DIALOG = "expired_dialog";

    private DataManager mDataManager;
    private String mVenueId;
    private FragmentVenueDetailBinding mBinding;
    private TokenStore mTokenStore;
    private VenueDetailViewModel mViewModel;

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
        super.onCreateView(inflater, container, savedInstanceState);
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_venue_detail, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        mVenueId = getArguments().getString(ARG_VENUE_ID);
        mDataManager = DataManager.get(getActivity());
        mDataManager.addVenueCheckInListener(this);
        mDataManager.addGetHoursListener(this);
        mViewModel = new VenueDetailViewModel(mDataManager.getVenue(mVenueId));
        mBinding.setVenueDetailViewModel(mViewModel);

        mDataManager.fetchVenueHours(mVenueId);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mTokenStore.getAccessToken() != null) {
            mViewModel.showCheckinButton(true);
            mBinding.setCheckinButtonClickListener(mCheckInClickListener);
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
        mViewModel.updateHours(hoursResponse.getOpenHours());
    }

    @Override
    public void onTokenExpired() {
        mViewModel.showCheckinButton(false);
        ExpiredTokenDialogFragment dialogFragment = new ExpiredTokenDialogFragment();
        dialogFragment.show(getFragmentManager(), EXPIRED_DIALOG);
    }
}
