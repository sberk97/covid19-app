package com.example.covid19countryinfo.misc;

import com.example.covid19countryinfo.models.Country;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public class Helper {

    public static String formatDate(Long epochTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.UK);
        return sdf.format(new Date(epochTime));
    }

    public static HashSet<String> turnCountryCodeStringToSet(String countryCodes) {
        HashSet<String> selectedCountriesCode = new HashSet<>();
        StringTokenizer st = new StringTokenizer(countryCodes, ",");
        while (st.hasMoreTokens()) {
            selectedCountriesCode.add(st.nextToken());
        }

        return selectedCountriesCode;
    }

    public static String getCountryCodesString(List<Country> mSelectedCountryList) {
        StringBuilder sb = new StringBuilder();
        for (Country country : mSelectedCountryList) {
            sb.append(country.getCountryCode()).append(",");
        }
        return sb.toString();
    }

    public static String shortenCountryName(String countryName) {
        int commaInCountryName = countryName.indexOf(',');
        if (commaInCountryName != -1) {
            countryName = countryName.substring(0, commaInCountryName);
        }
        return countryName;
    }
}
