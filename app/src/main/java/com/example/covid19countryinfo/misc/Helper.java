package com.example.covid19countryinfo.misc;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.covid19countryinfo.R;
import com.example.covid19countryinfo.fragments.EmptyListFragment;
import com.example.covid19countryinfo.models.SelectedListCountry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;

public class Helper {

    public static String formatDate(Long epochTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
        return sdf.format(new Date(epochTime));
    }

    public static HashSet<String> turnCountryCodeStringToSet(String countryCodes) {
        HashSet<String> selectedCountriesCode = new HashSet<>();
        StringTokenizer st = new StringTokenizer(countryCodes, ",");
        while(st.hasMoreTokens()) {
            selectedCountriesCode.add(st.nextToken());
        }

        return selectedCountriesCode;
    }

    public static String getCountryCodesString(List<SelectedListCountry> mSelectedCountryList) {
        StringBuilder sb = new StringBuilder();
        for(SelectedListCountry country : mSelectedCountryList) {
            sb.append(country.getCountryCode()).append(",");
        }
        return sb.toString();
    }

    public static boolean displayFragment(FragmentManager fragmentManager, int fragmentId) {
        EmptyListFragment emptyListFragment = EmptyListFragment.newInstance();

        // Get the FragmentManager and start a transaction.
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Add the SimpleFragment.
        fragmentTransaction.add(fragmentId, emptyListFragment).addToBackStack(null).commit();
        // Set boolean flag to indicate fragment is open.
        return true;
    }

    public static boolean hideFragment(FragmentManager fragmentManager, int fragmentId) {
        // Check to see if the fragment is already showing.
        EmptyListFragment emptyListFragment = (EmptyListFragment) fragmentManager.findFragmentById(fragmentId);
        if (emptyListFragment != null) {
            // Create and commit the transaction to remove the fragment.
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(emptyListFragment).commit();
        }

        // Set boolean flag to indicate fragment is closed.
        return false;
    }
}
