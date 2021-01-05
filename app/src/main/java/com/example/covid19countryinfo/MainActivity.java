package com.example.covid19countryinfo;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

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
import android.widget.Toast;


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
        StringBuffer buffer = new StringBuffer();
        while (c.moveToNext()) {
            buffer.append("Country: " + c.getString(0) + "\n");
            buffer.append("todayCases: " + c.getInt(1) + "\n");
            buffer.append("todayDeaths: " + c.getInt(2) + "\n");
            buffer.append("todayRecovered: " + c.getInt(3) + "\n");
            buffer.append("date: " + c.getString(4) + "\n\n");
        }

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