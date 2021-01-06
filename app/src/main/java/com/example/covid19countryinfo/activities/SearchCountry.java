package com.example.covid19countryinfo.activities;

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
import com.example.covid19countryinfo.R;
import com.example.covid19countryinfo.adapters.CountryListAdapter;
import com.example.covid19countryinfo.misc.Constants;
import com.example.covid19countryinfo.misc.DatabaseHelper;
import com.example.covid19countryinfo.misc.FetchCountryTask;
import com.example.covid19countryinfo.misc.RequestQueueSingleton;
import com.example.covid19countryinfo.models.SearchableCountry;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SearchCountry extends AppCompatActivity implements FetchCountryTask.OnCountryFetchCompleted, CountryListAdapter.OnCountryListener {

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

    private void saveCountryData(Map<String, String> dataMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ")
                .append(Constants.DATABASE_TABLE)
                .append(" VALUES('")
                .append(dataMap.get("countryName")).append("','")
                .append(Integer.parseInt(dataMap.get("todayCases"))).append("','")
                .append(Integer.parseInt(dataMap.get("todayDeaths"))).append("','")
                .append(Integer.parseInt(dataMap.get("todayRecovered"))).append("','")
                .append(dataMap.get("date")).append("');");
        mDb.execSQL(sb.toString());
    }

    private void onDataFetchError() {
        Toast.makeText(getApplicationContext(), R.string.data_fetch_error, Toast.LENGTH_SHORT).show();
    }

    public void getDataForGivenCountry(String countryCode) {
        String url = Constants.COUNTRY_DATA_API + countryCode;
        Map<String, String> dataMap = new HashMap<>();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    dataMap.put("countryName", response.getString("country"));
                    dataMap.put("todayCases", response.getString("todayCases"));
                    dataMap.put("todayDeaths", response.getString("todayDeaths"));
                    dataMap.put("todayRecovered", response.getString("todayRecovered"));
                    dataMap.put("date", formatDate(response.getLong("updated")));
                    saveCountryData(dataMap);
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