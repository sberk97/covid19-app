package com.example.covid19countryinfo.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.example.covid19countryinfo.R;
import com.example.covid19countryinfo.adapters.CountryListAdapter;
import com.example.covid19countryinfo.misc.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {
    private CountryListAdapter mAdapter;
    private SQLiteDatabase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DatabaseHelper sqLiteHelper = DatabaseHelper.getInstance(this);
        mDb = sqLiteHelper.getWritableDatabase();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchCountry.class);
                startActivity(intent);
            }
        });

        showContent();
    }

    public void showContent() {
        //mDb.execSQL("DROP TABLE IF EXISTS " + "recentCountryData");
        // Retrieving all records
        Cursor c = mDb.rawQuery("SELECT * FROM latest_country_data", null);

        // Checking if no records found
        if (c.getCount() == 0) {
            showMessage("Error", "No records found");
            return;
        }

        // Appending records to a string buffer
        StringBuilder buffer = new StringBuilder();
        while (c.moveToNext()) {
            buffer.append("Country: ").append(c.getString(0)).append("\n");
            buffer.append("todayCases: ").append(c.getInt(1)).append("\n");
            buffer.append("todayDeaths: ").append(c.getInt(2)).append("\n");
            buffer.append("todayRecovered: ").append(c.getInt(3)).append("\n");
            buffer.append("date: ").append(c.getString(4)).append("\n\n");
        }
        c.close();
        // Displaying all records
        showMessage("Country data", buffer.toString());
    }

    private void showMessage(String title, String message) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public void onClickShowAlert() {
        AlertDialog.Builder myAlertBuilder = new AlertDialog.Builder(MainActivity.this);
        myAlertBuilder.setTitle(R.string.action_about);
        StringBuilder sb = new StringBuilder();
        sb.append("Author: Sebastian Berk");
        sb.append("\n");
        sb.append("UB Number: 17009930");
        sb.append("\n");
        sb.append("COVID-19 Data from REST API: ");
        sb.append("\n");
        sb.append("https://disease.sh");
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
}