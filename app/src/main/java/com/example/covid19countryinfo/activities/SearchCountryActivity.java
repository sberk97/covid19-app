package com.example.covid19countryinfo.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.covid19countryinfo.R;
import com.example.covid19countryinfo.adapters.SearchCountryListAdapter;
import com.example.covid19countryinfo.misc.Constants;
import com.example.covid19countryinfo.misc.DatabaseHelper;
import com.example.covid19countryinfo.misc.FetchLocationTask;
import com.example.covid19countryinfo.misc.Helper;
import com.example.covid19countryinfo.misc.RequestQueueSingleton;
import com.example.covid19countryinfo.models.Country;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SearchCountryActivity extends AppCompatActivity implements FetchLocationTask.OnCountryFetchCompleted, SearchCountryListAdapter.OnSearchCountryListener {

    private List<Country> mCountryList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private SearchCountryListAdapter mAdapter;
    private FusedLocationProviderClient mFusedLocationClient;
    private MenuItem mSearch;
    private ProgressBar mProgressBar;
    private SQLiteDatabase mDb;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_country);

        mCountryList = getCountryList();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mProgressBar = (ProgressBar) findViewById(R.id.country_finding);

        DatabaseHelper sqLiteHelper = DatabaseHelper.getInstance(this);
        mDb = sqLiteHelper.getWritableDatabase();

        // Get a handle to the RecyclerView.
        mRecyclerView = findViewById(R.id.search_country_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new SearchCountryListAdapter(this, mCountryList, this);
        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.setOnTouchListener((v, event) -> {
            hideKeyboard();
            return false;
        });
    }

    @Override
    public void onCountryClick(int position) {
        hideKeyboard();

        mProgressBar.setVisibility(View.VISIBLE);
        String countryName = mCountryList.get(position).getCountryName();
        String countryCode = mCountryList.get(position).getCountryCode();
        getAndSaveDataForGivenCountry(countryName, countryCode, false);
    }

    private void onDataFetchError() {
        Toast.makeText(getApplicationContext(), R.string.data_fetch_error, Toast.LENGTH_SHORT).show();
    }

    private void getAndSaveDataForGivenCountry(String countryName, String countryCode, boolean getYesterdayData) {
        String url = Constants.COUNTRY_DATA_API + countryCode;
        if (getYesterdayData) {
            url += "?yesterday=true";
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                int latestCases = response.getInt("todayCases");
                int latestDeaths = response.getInt("todayDeaths");
                int latestRecovered = response.getInt("todayRecovered");
                boolean hasNoCases = latestCases == 0 && latestDeaths == 0 && latestRecovered == 0;
                if (hasNoCases && !getYesterdayData) {
                    getAndSaveDataForGivenCountry(countryName, countryCode, true);
                    return;
                }

                long epochDate = response.getLong("updated");
                if (getYesterdayData && !hasNoCases) {
                    epochDate -= 86400000;
                }

                StringBuilder sb = new StringBuilder();
                sb.append("INSERT INTO ")
                        .append(Constants.DATABASE_TABLE)
                        .append(" VALUES('")
                        .append(countryName.replace("'", "''")).append("','")
                        .append(countryCode).append("',")
                        .append(latestCases).append(",")
                        .append(latestDeaths).append(",")
                        .append(latestRecovered).append(",'")
                        .append(Helper.formatDate(epochDate)).append("');");
                mDb.execSQL(sb.toString());
                Intent returnIntent = getIntent();
                returnIntent.putExtra("addedCountry", countryCode);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } catch (JSONException e) {
                e.printStackTrace();
                onDataFetchError();
            }
        }, error -> {
            error.printStackTrace();
            onDataFetchError();
        });
        RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        mSearch = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) mSearch.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_locate:
                getLocation();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_LOCATION_PERMISSION);
        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(getOnSuccessListener());
        }
    }

    private OnSuccessListener<Location> getOnSuccessListener() {
        return new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    new FetchLocationTask(SearchCountryActivity.this, SearchCountryActivity.this).execute(location);
                } else {
                    Toast.makeText(getApplicationContext(), R.string.no_location, Toast.LENGTH_SHORT).show();
                }
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onLocationFetchCompleted(String result, boolean completedSuccessfully) {
        mProgressBar.setVisibility(View.INVISIBLE);
        if (completedSuccessfully) {
            mSearch.expandActionView();
            SearchView searchView = (SearchView) mSearch.getActionView();
            searchView.setQuery(result, false);
            searchView.clearFocus();
        } else {
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
        }
    }

    private List<Country> getCountryList() {
        HashSet<String> selectedCountriesCode = Helper.turnCountryCodeStringToSet(getIntent().getStringExtra(Constants.EXTRA_SELECTED_COUNTRIES));

        String[] countries = getResources().getStringArray(R.array.countries_array);
        List<Country> countryObjects = new ArrayList<>();
        for (String country : countries) {
            int lastComma = country.lastIndexOf(',');
            String countryName = country.substring(0, lastComma);
            String countryCode = country.substring(lastComma + 1);
            if (!selectedCountriesCode.contains(countryCode)) {
                countryObjects.add(new Country(countryName, countryCode));
            }
        }
        return countryObjects;
    }

    private void hideKeyboard() {
        SearchView searchView = (SearchView) mSearch.getActionView();
        searchView.clearFocus();
    }
}