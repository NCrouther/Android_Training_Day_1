package com.bignerdranch.android.networkingarchitecture.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bignerdranch.android.networkingarchitecture.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class VenueView extends LinearLayout implements Target {
    private TextView mTitleTextView;
    private TextView mAddressTextView;

    public VenueView(Context context) {
        this(context, null);
    }

    public VenueView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        LinearLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        setLayoutParams(params);

        LayoutInflater inflater = LayoutInflater.from(context);
        VenueView view = (VenueView) inflater.inflate(
                R.layout.view_venue, this, true);
        mTitleTextView = (TextView) view.findViewById(
                R.id.view_venue_list_VenueTitleTextView);
        mAddressTextView = (TextView) view.findViewById(
                R.id.view_venue_list_VenueLocationTextView);
    }

    public void setVenueTitle(String title) {
        mTitleTextView.setText(title);
    }

    public void setVenueAddress(String address) {
        mAddressTextView.setText(address);
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        Drawable image = new BitmapDrawable(getResources(), bitmap);
        makeImageAllBlack(image);
        mTitleTextView.setCompoundDrawablesWithIntrinsicBounds(
                image, null, null, null);
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        makeImageAllBlack(errorDrawable);
        mTitleTextView.setCompoundDrawablesWithIntrinsicBounds(
                errorDrawable, null, null, null);
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        makeImageAllBlack(placeHolderDrawable);
        mTitleTextView.setCompoundDrawablesWithIntrinsicBounds(
                placeHolderDrawable, null, null, null);
    }

    private void makeImageAllBlack(@Nullable Drawable image) {
        if (image != null) {
            image.mutate().setColorFilter(0xFF000000, PorterDuff.Mode.MULTIPLY);
        }
    }

    public void loadIcon(String iconPath) {
        Picasso.with(getContext()).load(iconPath).into(this);
    }

    public void clearIcon() {
        mTitleTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }
}
