package com.example.covid19countryinfo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchCountry extends AppCompatActivity implements FetchCountryTask.OnCountryFetchCompleted, CountryListAdapter.OnCountryListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String COUNTRY_DATA_API = "https://disease.sh/v3/covid-19/countries/";

    private List<SearchableCountry> mCountryList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private CountryListAdapter mAdapter;
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
        mAdapter = new CountryListAdapter(this, mCountryList, this);
        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onCountryClick(int position) {
        String countryCode = mCountryList.get(position).getCountryCode();
        getDataForGivenCountry(countryCode);
        //Toast.makeText(getApplicationContext(), String.valueOf(resp.size()), Toast.LENGTH_SHORT).show();

        //mCountryList.set(position, new SearchableCountry(element + " click! ", mCountryList.get(position).getCountryCode()));
        //mAdapter.notifyDataSetChanged();
    }

    private void saveCountryData(Map<String, Integer> dataMap, String countryName) {
        mDb.execSQL("INSERT INTO recentCountryData VALUES('" + countryName + "','" + dataMap.get("todayCases") +
                "','" + dataMap.get("todayDeaths") + "','" + dataMap.get("todayRecovered") + "','" + dataMap.get("date") +"');");
    }

    private void onDataFetchError() {
        Toast.makeText(getApplicationContext(), R.string.data_fetch_error, Toast.LENGTH_SHORT).show();
    }

    public void getDataForGivenCountry(String countryCode) {
        String url = COUNTRY_DATA_API + countryCode;
        Map<String, Integer> dataMap = new HashMap<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    dataMap.put("todayCases", response.getInt("todayCases"));
                    dataMap.put("todayDeaths", response.getInt("todayDeaths"));
                    dataMap.put("todayRecovered", response.getInt("todayRecovered"));
                    dataMap.put("date", response.getInt("updated"));
                    saveCountryData(dataMap, response.getString("country"));
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                    onDataFetchError();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                onDataFetchError();
            }
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
            default:
                // Do nothing
        }
        return super.onOptionsItemSelected(item);
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener( new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        mProgressBar.setVisibility(View.VISIBLE);
                        // Start the reverse geocode AsyncTask
                        new FetchCountryTask(SearchCountry.this, SearchCountry.this).execute(location);
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
            case REQUEST_LOCATION_PERMISSION:
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

    public List<SearchableCountry> getCountryList() {
        String[] countries = getResources().getStringArray(R.array.countries_array);
        List<SearchableCountry> countryObjects = new ArrayList<>();
        for(String country : countries) {
            int lastComma = country.lastIndexOf(',');
            countryObjects.add(new SearchableCountry(country.substring(0, lastComma), country.substring(lastComma+1)));
        }
        return countryObjects;
    }
}