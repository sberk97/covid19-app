package com.example.covid19countryinfo.misc;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.example.covid19countryinfo.R;
import com.example.covid19countryinfo.models.Country;

import java.util.ArrayList;
import java.util.List;

public class DatabaseOperations {

    public static List<Country> fetchCountries(String sql, Context context, SQLiteDatabase mDb) {
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
            Toast.makeText(context, R.string.error_country_retrieve, Toast.LENGTH_SHORT).show();
        }

        return retrievedCountries;
    }
}
