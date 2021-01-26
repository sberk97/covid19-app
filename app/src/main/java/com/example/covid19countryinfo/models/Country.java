package com.example.covid19countryinfo.models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.covid19countryinfo.misc.Constants;
import com.example.covid19countryinfo.misc.Helper;
import com.example.covid19countryinfo.misc.NoCasesException;
import com.example.covid19countryinfo.misc.RequestQueueSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Country {
    private String countryName;
    private String countryCode;
    private int latestCases;
    private int latestDeaths;
    private int latestRecovered;
    private String lastUpdateDate;

    public Country(String countryName, String countryCode) {
        this.countryName = countryName;
        this.countryCode = countryCode;
    }

    public Country(String countryName, String countryCode, int latestCases, int latestDeaths, int latestRecovered, String lastUpdateDate) {
        this.countryName = countryName;
        this.countryCode = countryCode;
        this.latestCases = latestCases;
        this.latestDeaths = latestDeaths;
        this.latestRecovered = latestRecovered;
        this.lastUpdateDate = lastUpdateDate;
    }

    public int getLatestCases() {
        return latestCases;
    }

    public void setLatestCases(int latestCases) {
        this.latestCases = latestCases;
    }

    public int getLatestDeaths() {
        return latestDeaths;
    }

    public void setLatestDeaths(int latestDeaths) {
        this.latestDeaths = latestDeaths;
    }

    public int getLatestRecovered() {
        return latestRecovered;
    }

    public void setLatestRecovered(int latestRecovered) {
        this.latestRecovered = latestRecovered;
    }

    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(String lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void update(boolean getYesterdayData, Context ctx, Response.Listener<JSONObject> responseListener, Response.ErrorListener errorListener) {
        String url = Constants.COUNTRY_DATA_API + countryCode;
        if (getYesterdayData) {
            url += "?yesterday=true";
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, responseListener, errorListener);
        RequestQueueSingleton.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
    }

    public boolean updateObject(boolean getYesterdayData, JSONObject response) throws JSONException, NoCasesException {
        int latestCases = response.getInt("todayCases");
        int latestDeaths = response.getInt("todayDeaths");
        int latestRecovered = response.getInt("todayRecovered");
        long epochDate = response.getLong("updated");

        boolean noNewCases = noNewCasesInLatestData(latestCases, latestDeaths, latestRecovered);
        boolean oldDataNoNewCases = noNewCasesInCurrentData();
        boolean dataIsTheSame = isCurrentAndLatestDataTheSame(latestCases, latestDeaths, latestRecovered, epochDate);

        if (dataIsTheSame) {
            return false;
        }
        // If today and yesterday no new cases
        else if (noNewCases && oldDataNoNewCases) {
            if (getYesterdayData) {
                epochDate = getYesterdayDate(epochDate);
                this.setLastUpdateDate(Helper.formatDate(epochDate));
            } else {
                throw new NoCasesException("Latest data does not have new cases");
            }
        }
        // Old data has cases, new doesn't and its not yesterday data then try getting yesterday data
        else if (noNewCases && !getYesterdayData) {
            throw new NoCasesException("Latest data does not have new cases");
        }
        // New data has cases
        else {
            if (getYesterdayData) {
                epochDate = getYesterdayDate(epochDate);
            }

            this.setLatestCases(latestCases);
            this.setLatestDeaths(latestDeaths);
            this.setLatestRecovered(latestRecovered);
            this.setLastUpdateDate(Helper.formatDate(epochDate));
        }

        return true;
    }

    public void updateDataDatabase(SQLiteDatabase mDb) {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.UPDATE_COUNTRY)
                .append("latest_cases=")
                .append(latestCases)
                .append(", latest_deaths=")
                .append(latestDeaths)
                .append(", latest_recovered=")
                .append(latestRecovered)
                .append(", date='")
                .append(dateToEpoch())
                .append("' WHERE country_code='")
                .append(countryCode).append("';");

        mDb.execSQL(sb.toString());
    }

    private static boolean noNewCasesInLatestData(int latestCases, int latestDeaths, int latestRecovered) {
        return latestCases == 0 && latestDeaths == 0 && latestRecovered == 0;
    }

    private boolean noNewCasesInCurrentData() {
        return this.getLatestCases() == 0 && this.getLatestDeaths() == 0 && this.getLatestRecovered() == 0;
    }

    private boolean isCurrentAndLatestDataTheSame(int latestCases, int latestDeaths, int latestRecovered, long epochDate) {
        return this.getLatestCases() == latestCases && this.getLatestDeaths() == latestDeaths && this.getLatestRecovered() == latestRecovered && this.getLastUpdateDate().equals(Helper.formatDate(epochDate));
    }

    private long getYesterdayDate(long epochDate) {
        return epochDate - 86400000;
    }

    private long dateToEpoch() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
            Date date = sdf.parse(lastUpdateDate);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
