package com.example.covid19countryinfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.LinkedList;

public class SearchCountry extends AppCompatActivity implements CountryListAdapter.OnCountryListener {

    private final ArrayList<String[]> mCountryList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private CountryListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_country);

        mCountryList.add(new String[]{"United Kingdom", "UK"});
        mCountryList.add(new String[]{"Poland", "PL"});

        // Get a handle to the RecyclerView.
        mRecyclerView = findViewById(R.id.recyclerview);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new CountryListAdapter(this, mCountryList, this);
        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onCountryClick(int position) {
        String element = mCountryList.get(position)[0];
        mCountryList.set(position, new String[]{"Clicked! " + element, mCountryList.get(position)[1]});
        mAdapter.notifyDataSetChanged();
    }
}