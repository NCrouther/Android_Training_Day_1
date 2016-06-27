package com.bignerdranch.android.networkingarchitecture.controller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bignerdranch.android.networkingarchitecture.R;

public class VenueListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_venue_list);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer, new VenueListFragment())
                .commit();
    }
}
