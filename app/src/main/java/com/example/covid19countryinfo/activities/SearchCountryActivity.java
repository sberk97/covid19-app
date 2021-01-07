package com.example.covid19countryinfo.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.covid19countryinfo.R;
import com.example.covid19countryinfo.adapters.SearchCountryListAdapter;
import com.example.covid19countryinfo.misc.Constants;
import com.example.covid19countryinfo.misc.DatabaseHelper;
import com.example.covid19countryinfo.misc.FetchCountryTask;
import com.example.covid19countryinfo.misc.RequestQueueSingleton;
import com.example.covid19countryinfo.models.SearchListCountry;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

public class SearchCountryActivity extends AppCompatActivity implements FetchCountryTask.OnCountryFetchCompleted, SearchCountryListAdapter.OnSearchCountryListener {

    private List<SearchListCountry> mCountryList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private SearchCountryListAdapter mAdapter;
    private FusedLocationProviderClient mFusedLocationClient;
    private MenuItem mSearch;
    private ProgressBar mProgressBar;
    private SQLiteDatabase mDb;

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
        mRecyclerView = findViewById(R.id.recyclerview);
        mRecyclerView.setHasFixedSize(true);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new SearchCountryListAdapter(this, mCountryList, this);
        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
//        mRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public void onCountryClick(int position) {
        mProgressBar.setVisibility(View.VISIBLE);
        String countryName = mCountryList.get(position).getCountryName();
        String countryCode = mCountryList.get(position).getCountryCode();
        getDataForGivenCountry(countryName, countryCode, false);
    }

    private void saveCountryData(String sql) {
        mDb.execSQL(sql);
    }

    private void onDataFetchError() {
        Toast.makeText(getApplicationContext(), R.string.data_fetch_error, Toast.LENGTH_SHORT).show();
    }

    public void getDataForGivenCountry(String countryName, String countryCode, boolean getYesterdayData) {
        String url = Constants.COUNTRY_DATA_API + countryCode;
        if(getYesterdayData) {
            url += "?yesterday=true";
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                int todayCases = response.getInt("todayCases");
                int todayDeaths = response.getInt("todayDeaths");
                int todayRecovered = response.getInt("todayRecovered");
                boolean hasNoCases = todayCases==0 && todayDeaths==0 && todayRecovered==0;
                if (hasNoCases && !getYesterdayData) {
                    getDataForGivenCountry(countryName, countryCode, true);
                    return;
                }

                Long epochDate = response.getLong("updated");
                if(getYesterdayData && !hasNoCases) {
                    epochDate -= 86400000;
                }

                StringBuilder sb = new StringBuilder();
                sb.append("INSERT INTO ")
                        .append(Constants.DATABASE_TABLE)
                        .append(" VALUES('")
                        .append(countryName.replace("'", "''")).append("','")
                        .append(countryCode).append("',")
                        .append(todayCases).append(",")
                        .append(todayDeaths).append(",")
                        .append(todayRecovered).append(",'")
                        .append(formatDate(epochDate)).append("');");
                saveCountryData(sb.toString());
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

    private String formatDate(Long epochTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
        return sdf.format(new Date(epochTime));
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
            default:
                // Do nothing
        }
        return super.onOptionsItemSelected(item);
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, Constants.REQUEST_LOCATION_PERMISSION);
        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener( new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        // Start the reverse geocode AsyncTask
                        new FetchCountryTask(SearchCountryActivity.this, SearchCountryActivity.this).execute(location);
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.no_location, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_LOCATION_PERMISSION:
                // If the permission is granted, get the location,
                // otherwise, show a Toast
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onCountryFetchCompleted(String result, boolean completedSuccessfully) {
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

    public List<SearchListCountry> getCountryList() {
        HashSet<String> selectedCountriesCode = new HashSet<>();
        StringTokenizer st = new StringTokenizer(getIntent().getStringExtra(Constants.EXTRA_SELECTED_COUNTRIES), ",");
        while(st.hasMoreTokens()) {
            selectedCountriesCode.add(st.nextToken());
        }

        String[] countries = getResources().getStringArray(R.array.countries_array);
        List<SearchListCountry> countryObjects = new ArrayList<>();
        for(String country : countries) {
            int lastComma = country.lastIndexOf(',');
            String countryName = country.substring(0, lastComma);
            String countryCode = country.substring(lastComma+1);
            if (!selectedCountriesCode.contains(countryCode)) {
                countryObjects.add(new SearchListCountry(countryName, countryCode));
            }
        }
        return countryObjects;
    }
}