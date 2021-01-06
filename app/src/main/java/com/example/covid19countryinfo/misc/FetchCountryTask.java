package com.example.covid19countryinfo.misc;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.example.covid19countryinfo.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FetchCountryTask extends AsyncTask<Location, Void, String> {
    private final String TAG = FetchCountryTask.class.getSimpleName();
    private Context mContext;
    private OnCountryFetchCompleted mListener;
    private boolean completedSuccessfully = false;

    public FetchCountryTask(Context applicationContext, OnCountryFetchCompleted listener) {
        mContext = applicationContext;
        mListener = listener;
    }

    @Override
    protected String doInBackground(Location... locations) {
        Geocoder geocoder = new Geocoder(mContext, Locale.UK);
        Location location = locations[0];
        List<Address> addresses = new ArrayList<>();
        String resultMessage = "";

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems
            resultMessage = mContext.getString(R.string.service_not_available);
            Log.e(TAG, resultMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values
            resultMessage = mContext.getString(R.string.invalid_lat_long_used);
            Log.e(TAG, resultMessage + ". " + "Latitude = " + location.getLatitude() + ", Longitude = " + location.getLongitude(), illegalArgumentException);
        }

        if ((addresses == null || addresses.size() == 0) && resultMessage.isEmpty()) {
            resultMessage = mContext.getString(R.string.no_address_found);
            Log.e(TAG, resultMessage);
        } else {
            resultMessage = addresses.get(0).getCountryName(); //+ " " + addresses.get(0).getCountryCode();
            completedSuccessfully = true;
        }
        return resultMessage;
    }

    @Override
    protected void onPostExecute(String address) {
        mListener.onCountryFetchCompleted(address, completedSuccessfully);
        super.onPostExecute(address);
    }

    public interface OnCountryFetchCompleted {
        void onCountryFetchCompleted(String result, boolean completedSuccessfully);
    }
}
