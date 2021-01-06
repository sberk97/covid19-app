package com.example.covid19countryinfo.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.example.covid19countryinfo.R;
import com.example.covid19countryinfo.adapters.SelectedCountryListAdapter;
import com.example.covid19countryinfo.misc.Constants;
import com.example.covid19countryinfo.misc.DatabaseHelper;
import com.example.covid19countryinfo.models.SelectedListCountry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SelectedCountryListAdapter.OnSelectedCountryListener {
    private SQLiteDatabase mDb;

    private SelectedCountryListAdapter mAdapter;
    private List<SelectedListCountry> mSelectedCountryList = new ArrayList<>();
    private StringBuilder mSelectedCountryISO = new StringBuilder();
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DatabaseHelper sqLiteHelper = DatabaseHelper.getInstance(this);
        mDb = sqLiteHelper.getWritableDatabase();

        getSelectedCountries();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchCountryActivity.class);
                intent.putExtra(Constants.EXTRA_SELECTED_COUNTRIES, mSelectedCountryISO.toString());
                startActivityForResult(intent, Constants.LAUNCH_SECOND_ACTIVITY);
            }
        });
        setUpRecyclerView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.LAUNCH_SECOND_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK){
                String addedCountry = data.getStringExtra("addedCountry");
                addNewCountry(addedCountry);
                mAdapter.notifyDataSetChanged();
            }
//            if (resultCode == Activity.RESULT_CANCELED) {
//                //Write your code if there's no result
//            }
        }
    }

    private void setUpRecyclerView() {
        // Get a handle to the RecyclerView.
        mRecyclerView = findViewById(R.id.main_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new SelectedCountryListAdapter(this, mSelectedCountryList, this);
        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void getSelectedCountries() {
        fetchCountries(Constants.GET_ALL_COUNTRIES);
    }

    private void addNewCountry(String countryCode) {
        fetchCountries(Constants.GET_GIVEN_COUNTRY + countryCode + "';");
    }

    private void fetchCountries(String sql) {
        Cursor c = mDb.rawQuery(sql, null);
        if(c.getCount() == 0) {
            return;
        }
        while (c.moveToNext()) {
            String countryName = c.getString(0);
            String countryCode = c.getString(1);
            int todayCases = c.getInt(2);
            int todayDeaths = c.getInt(3);
            int todayRecovered = c.getInt(4);
            String lastUpdateDate = c.getString(5);
            mSelectedCountryISO.append(",").append(countryCode);
            mSelectedCountryList.add(new SelectedListCountry(countryName, countryCode, todayCases, todayDeaths, todayRecovered, lastUpdateDate));
        }
        c.close();
    }

    public void onClickShowAlert() {
        AlertDialog.Builder myAlertBuilder = new AlertDialog.Builder(MainActivity.this);
        myAlertBuilder.setTitle(R.string.action_about);
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.author))
        .append("\n")
        .append(getString(R.string.ub_number))
        .append("\n")
        .append(getString(R.string.data_from))
        .append("\n")
        .append(Constants.COUNTRY_DATA_API_DOMAIN);
        final SpannableString s = new SpannableString(sb.toString()); // msg should have url to enable clicking
        Linkify.addLinks(s, Linkify.ALL);
        myAlertBuilder.setMessage(s);
        myAlertBuilder.setPositiveButton(R.string.ok, null);
        ((TextView)myAlertBuilder.show().findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                onClickShowAlert();
                return true;
            default:
                // Do nothing
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCountryClick(int position) {
        // move to country activity
    }
}