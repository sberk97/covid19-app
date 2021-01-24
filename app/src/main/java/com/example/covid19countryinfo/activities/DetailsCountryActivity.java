package com.example.covid19countryinfo.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.covid19countryinfo.R;
import com.example.covid19countryinfo.misc.Constants;
import com.example.covid19countryinfo.misc.Helper;
import com.example.covid19countryinfo.misc.RequestQueueSingleton;

import org.json.JSONException;
import org.json.JSONObject;

public class DetailsCountryActivity extends AppCompatActivity {

    private TextView lastUpdateView;

    private TextView totalCasesView;
    private TextView totalDeathsView;
    private TextView totalRecoveredView;
    private TextView totalActiveView;

    private TextView todayCasesView;
    private TextView todayDeathsView;
    private TextView todayRecoveredView;

    private TextView criticalPatientsView;
    private TextView testsPerformedView;
    private TextView populationView;

    private FrameLayout mProgressBarFrame;
    private String countryCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_country);
        countryCode = getIntent().getStringExtra(Constants.EXTRA_CLICKED_COUNTRY_CODE);
        mProgressBarFrame = (FrameLayout) findViewById(R.id.country_details_loading_frame);

        TextView countryNameView = findViewById(R.id.details_country_name);
        countryNameView.setText(getIntent().getStringExtra(Constants.EXTRA_CLICKED_COUNTRY_NAME));
        lastUpdateView = findViewById(R.id.details_country_last_update);

        totalCasesView = findViewById(R.id.details_country_total_cases);
        totalDeathsView = findViewById(R.id.details_country_total_deaths);
        totalRecoveredView = findViewById(R.id.details_country_total_recovered);
        totalActiveView = findViewById(R.id.details_country_active);

        todayCasesView = findViewById(R.id.details_country_today_cases);
        todayDeathsView = findViewById(R.id.details_country_today_deaths);
        todayRecoveredView = findViewById(R.id.details_country_today_recovered);

        criticalPatientsView = findViewById(R.id.details_country_critical);
        testsPerformedView = findViewById(R.id.details_country_tests);
        populationView = findViewById(R.id.details_country_population);

        getCountryData(false);
    }

    private void getCountryData(boolean getYesterdayData) {
        displayProgressBarAndBlockTouch();
        String url = Constants.COUNTRY_DATA_API + countryCode;
        if (getYesterdayData) {
            url += "?yesterday=true";
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                appendDataToView(response, getYesterdayData);

                hideProgressBarAndUnlockTouch();
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

    private void appendDataToView(JSONObject response, boolean isYesterdayData) throws JSONException {
        int totalCases = response.getInt("cases");
        int totalDeaths = response.getInt("deaths");
        int totalRecovered = response.getInt("recovered");
        int totalActive = response.getInt("active");

        int latestCases = response.getInt("todayCases");
        int latestDeaths = response.getInt("todayDeaths");
        int latestRecovered = response.getInt("todayRecovered");

        int criticalPatients = response.getInt("critical");
        int testsPerformed = response.getInt("tests");
        int population = response.getInt("population");

        boolean hasNoCases = latestCases == 0 && latestDeaths == 0 && latestRecovered == 0;
        if (hasNoCases && !isYesterdayData) {
            getCountryData(true);
            return;
        }

        long epochDate = response.getLong("updated");
        if (isYesterdayData && !hasNoCases) {
            epochDate -= 86400000;
        }

        String lastUpdate = Helper.formatDate(epochDate);
        lastUpdateView.setText(lastUpdate);

        totalCasesView.setText(String.valueOf(totalCases));
        totalDeathsView.setText(String.valueOf(totalDeaths));
        totalRecoveredView.setText(String.valueOf(totalRecovered));
        totalActiveView.setText(String.valueOf(totalActive));

        todayCasesView.setText(String.valueOf(latestCases));
        todayDeathsView.setText(String.valueOf(latestDeaths));
        todayRecoveredView.setText(String.valueOf(latestRecovered));

        criticalPatientsView.setText(String.valueOf(criticalPatients));
        testsPerformedView.setText(String.valueOf(testsPerformed));
        populationView.setText(String.valueOf(population));
    }

    private void onDataFetchError() {
        hideProgressBarAndUnlockTouch();
        Toast.makeText(getApplicationContext(), R.string.data_fetch_error, Toast.LENGTH_SHORT).show();
    }

    private void displayProgressBarAndBlockTouch() {
        if(!mProgressBarFrame.isShown()) {
            mProgressBarFrame.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    private void hideProgressBarAndUnlockTouch() {
        mProgressBarFrame.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}