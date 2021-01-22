package com.example.covid19countryinfo.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.example.covid19countryinfo.R;
import com.example.covid19countryinfo.fragments.CountryListFragment;
import com.example.covid19countryinfo.fragments.EmptyListFragment;
import com.example.covid19countryinfo.misc.Constants;
import com.example.covid19countryinfo.misc.DatabaseHelper;
import com.example.covid19countryinfo.misc.DatabaseOperations;
import com.example.covid19countryinfo.misc.Helper;
import com.example.covid19countryinfo.models.Country;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private SQLiteDatabase mDb;
    private CountryListFragment countryListFragment;
    private MenuItem updateAllMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DatabaseHelper sqLiteHelper = DatabaseHelper.getInstance(this);
        mDb = sqLiteHelper.getWritableDatabase();

        if (DatabaseOperations.fetchCountries(Constants.GET_ALL_COUNTRIES, getApplicationContext(), mDb).isEmpty()) {
            displayEmptyListFragment();
        } else {
            displayCountryListFragment();
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> goToSearchCountryActivity());
    }

    public void displayCountryListFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        removeEmptyListFragment(fragmentManager);

        countryListFragment = new CountryListFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.country_list_fragment, countryListFragment).commit();
    }

    private void removeCountryListFragment(FragmentManager fragmentManager) {
        if (countryListFragment != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(countryListFragment).commit();
            updateAllMenuItem.setVisible(false);
        }
    }

    public void displayEmptyListFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        removeCountryListFragment(fragmentManager);

        EmptyListFragment fragment = new EmptyListFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.empty_list_fragment, fragment).commit();
    }

    private void removeEmptyListFragment(FragmentManager fragmentManager) {
        EmptyListFragment fragment = (EmptyListFragment) fragmentManager.findFragmentById(R.id.empty_list_fragment);
        if (fragment != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(fragment).commit();
            updateAllMenuItem.setVisible(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.LAUNCH_SECOND_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                String addedCountry = data.getStringExtra("addedCountry");
                if (countryListFragment == null || !countryListFragment.isVisible()) {
                    displayCountryListFragment();
                } else {
                    countryListFragment.addCountryToList(addedCountry);
                }

            }
//            if (resultCode == Activity.RESULT_CANCELED) {
//                //Write your code if there's no result
//            }
        }
    }

    private void goToSearchCountryActivity() {
        List<Country> selectedCountryList = new ArrayList<>();
        if (countryListFragment != null) {
            selectedCountryList = countryListFragment.getSelectedCountryList();
        }

        Intent intent = new Intent(MainActivity.this, SearchCountryActivity.class);
        intent.putExtra(Constants.EXTRA_SELECTED_COUNTRIES, Helper.getCountryCodesString(selectedCountryList));
        startActivityForResult(intent, Constants.LAUNCH_SECOND_ACTIVITY);
    }

    public void onClickShowAbout() {
        AlertDialog.Builder myAlertBuilder = new AlertDialog.Builder(MainActivity.this);
        myAlertBuilder.setTitle(R.string.action_about);
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.author))
                .append("\n")
                .append(getString(R.string.ub_number))
                .append("\n")
                .append(getString(R.string.data_from))
                .append("\n")
                .append(Constants.COUNTRY_DATA_API_DOMAIN);
        final SpannableString s = new SpannableString(sb.toString()); // msg should have url to enable clicking
        Linkify.addLinks(s, Linkify.ALL);
        myAlertBuilder.setMessage(s);
        myAlertBuilder.setPositiveButton(R.string.ok, null);
        ((TextView) myAlertBuilder.show().findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (countryListFragment == null) {
            updateAllMenuItem = menu.findItem(R.id.action_update_all);
            updateAllMenuItem.setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                onClickShowAbout();
                return true;
            case R.id.action_add_country:
                goToSearchCountryActivity();
                return true;
            case R.id.action_update_all:
                countryListFragment.updateAllCountries();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showCountryCardSettings(View view) {
        countryListFragment.showCountryCardSettings(view);
    }

}