package com.bignerdranch.android.networkingarchitecture.view;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bignerdranch.android.networkingarchitecture.controller.VenueDetailActivity;
import com.bignerdranch.android.networkingarchitecture.model.Venue;
import com.squareup.picasso.Picasso;

public class VenueHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    private static final int ICON_SIZE = 64;

    private VenueView mVenueView;
    private Venue mVenue;

    public VenueHolder(View itemView) {
        super(itemView);

        mVenueView = (VenueView) itemView;
        mVenueView.setOnClickListener(this);
    }

    public void bindVenue(Venue venue) {
        mVenue = venue;
        mVenueView.setVenueTitle(mVenue.getName());
        mVenueView.setVenueAddress(mVenue.getFormattedAddress());

        String iconPath = venue.getPrimaryCategoryIconPath(ICON_SIZE);
        if (iconPath != null) {
            mVenueView.loadIcon(iconPath);
        } else {
            mVenueView.clearIcon();
        }
    }

    @Override
    public void onClick(View view) {
        Context context = view.getContext();
        Intent intent = VenueDetailActivity.newIntent(context, mVenue.getId());
        context.startActivity(intent);
    }
}

