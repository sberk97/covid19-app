package com.example.covid19countryinfo.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.covid19countryinfo.R;
import com.example.covid19countryinfo.adapters.SelectedCountryListAdapter;
import com.example.covid19countryinfo.fragments.CountryListFragment;
import com.example.covid19countryinfo.fragments.EmptyListFragment;
import com.example.covid19countryinfo.misc.Constants;
import com.example.covid19countryinfo.misc.DatabaseHelper;
import com.example.covid19countryinfo.misc.Helper;
import com.example.covid19countryinfo.misc.RequestQueueSingleton;
import com.example.covid19countryinfo.models.Country;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SelectedCountryListAdapter.OnSelectedCountryListener {
    private SQLiteDatabase mDb;

//    private SelectedCountryListAdapter mAdapter;
//    private List<Country> mSelectedCountryList = new ArrayList<>();
//    private RecyclerView mRecyclerView;
    private boolean isEmptyListFragmentDisplayed = false;
    private CountryListFragment countryListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DatabaseHelper sqLiteHelper = DatabaseHelper.getInstance(this);
        mDb = sqLiteHelper.getWritableDatabase();

        if(fetchCountries(Constants.GET_ALL_COUNTRIES).isEmpty()) {
            displayEmptyListFragment();
        } else {
            displayCountryListFragment();
        }

//
//        setUpCountryList();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> goToSearchCountryActivity());

//        setUpRecyclerView();

        //setVisibilityOnScreen();

//        if (countryListFragment.getAdapter().getItemCount() == 0) {
//            Log.d("countryList empty", "empty");
//            displayEmptyListFragment();
//        }
    }

    public void displayCountryListFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        removeEmptyListFragment(fragmentManager);

        countryListFragment = new CountryListFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.country_list_fragment, countryListFragment).commit();
    }

    private void removeEmptyListFragment(FragmentManager fragmentManager) {
        EmptyListFragment fragment = (EmptyListFragment) fragmentManager.findFragmentById(R.id.empty_list_fragment);
        if (fragment != null) {
            // Create and commit the transaction to remove the fragment.
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(fragment).commit();
        }
    }

    public void displayEmptyListFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        removeCountryListFragment(fragmentManager);

        EmptyListFragment fragment = new EmptyListFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.empty_list_fragment, fragment).commit();
    }

    private void removeCountryListFragment(FragmentManager fragmentManager) {
//        CountryListFragment fragment = (CountryListFragment) fragmentManager.findFragmentById(R.id.country_list_fragment);
        if (countryListFragment != null) {
            // Create and commit the transaction to remove the fragment.
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(countryListFragment).commit();
        }
    }

//    private void setVisibilityOnScreen() {
//        if (mSelectedCountryList.isEmpty()) {
//            mRecyclerView.setVisibility(View.GONE);
//            isEmptyListFragmentDisplayed = Helper.displayFragment(getSupportFragmentManager(), R.id.empty_list_fragment);
//        } else {
//            mRecyclerView.setVisibility(View.VISIBLE);
//            isEmptyListFragmentDisplayed = Helper.hideFragment(getSupportFragmentManager(), R.id.empty_list_fragment);
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.LAUNCH_SECOND_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                String addedCountry = data.getStringExtra("addedCountry");
                if(countryListFragment == null || !countryListFragment.isVisible()) {
                    Log.d("country added null", addedCountry);
                    displayCountryListFragment();
                } else {
                    Log.d("country added not null", addedCountry);
                    countryListFragment.addCountryToList(addedCountry);

                }
//                countryListFragment.addCountryToList(addedCountry);
                //setVisibilityOnScreen();
//                mAdapter.notifyDataSetChanged();
            }
//            if (resultCode == Activity.RESULT_CANCELED) {
//                //Write your code if there's no result
//            }
        }
    }

//    private void setUpRecyclerView() {
//        // Get a handle to the RecyclerView.
//        mRecyclerView = findViewById(R.id.main_recycler_view);
//        mRecyclerView.setHasFixedSize(true);
//        // Create an adapter and supply the data to be displayed.
//        mAdapter = new SelectedCountryListAdapter(this, mSelectedCountryList, this);
//        // Connect the adapter with the RecyclerView.
//        mRecyclerView.setAdapter(mAdapter);
//        // Give the RecyclerView a default layout manager.
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//    }

    private void goToSearchCountryActivity() {
        List<Country> selectedCountryList = new ArrayList<>();
        if (countryListFragment != null) {
            selectedCountryList = countryListFragment.getSelectedCountryList();
        }

        Intent intent = new Intent(MainActivity.this, SearchCountryActivity.class);
        intent.putExtra(Constants.EXTRA_SELECTED_COUNTRIES, Helper.getCountryCodesString(selectedCountryList));
        startActivityForResult(intent, Constants.LAUNCH_SECOND_ACTIVITY);
    }

//    private void setUpCountryList() {
//        mSelectedCountryList = fetchCountries(Constants.GET_ALL_COUNTRIES);
//    }

//    private void addCountryToList(String countryCode) {
//        mSelectedCountryList.add(fetchCountries(Constants.GET_GIVEN_COUNTRY + countryCode + "';").get(0));
//    }

//    private void updateCountryInList(String countryCode, int countryListIndex) {
//        String sql = Constants.GET_GIVEN_COUNTRY + countryCode + "';";
//        List<Country> newCountryData = fetchCountries(sql);
//        mSelectedCountryList.set(countryListIndex, newCountryData.get(0));
//    }
//
//    private void removeCountry(int countryClicked) {
//        String countryCode = mSelectedCountryList.get(countryClicked).getCountryCode();
//        try {
//            mDb.execSQL(Constants.REMOVE_COUNTRY + countryCode + "'");
//            mSelectedCountryList.remove(countryClicked);
//            mAdapter.notifyDataSetChanged();
//        } catch (SQLException e) {
//            Toast.makeText(getApplicationContext(), R.string.error_country_remove, Toast.LENGTH_SHORT).show();
//        }
//    }

    private List<Country> fetchCountries(String sql) {
        List<Country> retrievedCountries = new ArrayList<>();
        try {
            Cursor c = mDb.rawQuery(sql, null);

            if (c.getCount() == 0) {
                return retrievedCountries;
            }

            while (c.moveToNext()) {
                String countryName = c.getString(0);
                String countryCode = c.getString(1);
                int latestCases = c.getInt(2);
                int latestDeaths = c.getInt(3);
                int latestRecovered = c.getInt(4);
                String lastUpdateDate = c.getString(5);
                retrievedCountries.add(new Country(countryName, countryCode, latestCases, latestDeaths, latestRecovered, lastUpdateDate));
            }
            c.close();
        } catch (SQLException e) {
            Toast.makeText(getApplicationContext(), R.string.error_country_retrieve, Toast.LENGTH_SHORT).show();
        }

        return retrievedCountries;
    }

    public void onClickShowAbout() {
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
        ((TextView) myAlertBuilder.show().findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                onClickShowAbout();
                return true;
            case R.id.action_add_country:
                goToSearchCountryActivity();
                return true;
            case R.id.action_update_all:
//                updateAllCountries();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCountryClick(int position) {
        // move to country activity
    }

    public void showCountryCardSettings(View view) {
        countryListFragment.showCountryCardSettings(view);
    }

//    public void showCountryCardSettings(View view) {
//        PopupMenu popup = new PopupMenu(this, view);
//        popup.getMenuInflater().inflate(R.menu.selected_country_menu, popup.getMenu());
//        popup.setOnMenuItemClickListener(getOnMenuItemClickListener(view));
//        popup.show();
//    }
//
//    @SuppressLint("NonConstantResourceId")
//    private PopupMenu.OnMenuItemClickListener getOnMenuItemClickListener(View view) {
//        return new PopupMenu.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                CardView cView = (CardView) ((ViewGroup) view.getParent()).getParent();
//                int countryClicked = mRecyclerView.getChildAdapterPosition(cView);
//                switch (item.getItemId()) {
//                    case R.id.action_remove:
//                        removeCountry(countryClicked);
//                        return true;
//                    case R.id.action_update_item:
//                        updateGivenCountry(countryClicked);
//                        return true;
//                }
//                return true;
//            }
//        };
//    }
//
//    private void updateAllCountries() {
//        String countryCodes = Helper.getCountryCodesString(mSelectedCountryList);
//        int i = 0;
//        for (Country country : mSelectedCountryList) {
//            getDataFromApiAndUpdate(country, i, false);
//            i++;
//        }
//    }
//
//    private void updateGivenCountry(int countryClicked) {
//        Country country = mSelectedCountryList.get(countryClicked);
//        getDataFromApiAndUpdate(country, countryClicked, false);
//    }
//
//    public void getDataFromApiAndUpdate(Country country, int countryListIndex, boolean getYesterdayData) {
//        String url = Constants.COUNTRY_DATA_API + country.getCountryCode();
//        if (getYesterdayData) {
//            url += "?yesterday=true";
//        }
//
//        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
//            try {
//                StringBuilder sb = getUpdateSQL(country, countryListIndex, getYesterdayData, response);
//                if (sb == null) return;
//
//                mDb.execSQL(sb.toString());
//                updateCountryInList(country.getCountryCode(), countryListIndex);
//                mAdapter.notifyDataSetChanged();
//            } catch (JSONException e) {
//                e.printStackTrace();
//                onDataFetchError();
//            } catch (SQLException e) {
//                Toast.makeText(getApplicationContext(), R.string.update_failed, Toast.LENGTH_SHORT).show();
//            }
//        }, error -> {
//            error.printStackTrace();
//            onDataFetchError();
//        });
//        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
//    }
//
//    private StringBuilder getUpdateSQL(Country country, int countryListIndex, boolean getYesterdayData, JSONObject response) throws JSONException {
//        int latestCases = response.getInt("todayCases");
//        int latestDeaths = response.getInt("todayDeaths");
//        int latestRecovered = response.getInt("todayRecovered");
//        long epochDate = response.getLong("updated");
//
//        boolean noNewCases = latestCases == 0 && latestDeaths == 0 && latestRecovered == 0;
//        boolean oldDataNoNewCases = country.getLatestCases() == 0 && country.getLatestDeaths() == 0 && country.getLatestRecovered() == 0;
//        boolean dataIsTheSame = country.getLatestCases() == latestCases && country.getLatestDeaths() == latestDeaths && country.getLatestRecovered() == latestRecovered && country.getLastUpdateDate().equals(Helper.formatDate(epochDate));
//        StringBuilder sb = new StringBuilder();
//
//        if (dataIsTheSame) {
//            return null;
//        }
//        // If today and yesterday no new cases
//        else if (noNewCases && oldDataNoNewCases) {
//            if (getYesterdayData) {
//                epochDate -= 86400000;
//                country.setLastUpdateDate(Helper.formatDate(epochDate));
//                sb.append(Constants.UPDATE_COUNTRY)
//                        .append("date='")
//                        .append(Helper.formatDate(epochDate))
//                        .append("' WHERE country_code='")
//                        .append(country.getCountryCode()).append("';");
//            } else {
//                getDataFromApiAndUpdate(country, countryListIndex, true);
//                return null;
//            }
//        }
//        // Old data has cases, new doesn't and its not yesterday data then try getting yesterday data
//        else if (noNewCases && !getYesterdayData) {
//            getDataFromApiAndUpdate(country, countryListIndex, true);
//            return null;
//        }
//        // New data has cases
//        else {
//            if (getYesterdayData) {
//                epochDate -= 86400000;
//            }
//
//            sb.append(Constants.UPDATE_COUNTRY)
//                    .append("latest_cases=")
//                    .append(latestCases)
//                    .append(", latest_deaths=")
//                    .append(latestDeaths)
//                    .append(", latest_recovered=")
//                    .append(latestRecovered)
//                    .append(", date='")
//                    .append(Helper.formatDate(epochDate))
//                    .append("' WHERE country_code='")
//                    .append(country.getCountryCode()).append("';");
//        }
//        return sb;
//    }
//
//    private void onDataFetchError() {
//        Toast.makeText(getApplicationContext(), R.string.data_fetch_error, Toast.LENGTH_SHORT).show();
//    }

}