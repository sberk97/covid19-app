package com.example.covid19countryinfo.misc;

public final class Constants {
    public static final int REQUEST_LOCATION_PERMISSION = 1;

    public static final String COUNTRY_DATA_API_DOMAIN = "https://disease.sh";

    public static final String COUNTRY_DATA_API_ENDPOINT = "/v3/covid-19/countries/";

    public static final String COUNTRY_DATA_API = COUNTRY_DATA_API_DOMAIN + COUNTRY_DATA_API_ENDPOINT;

    public static final String DATABASE_NAME = "covid19data";

    public static final String DATABASE_TABLE = "latest_country_data";

    public static final int DATABASE_VERSION = 8;

    public static final int LAUNCH_SECOND_ACTIVITY = 1;

    public static final String EXTRA_SELECTED_COUNTRIES = "EXTRA_SELECTED_COUNTRIES";

    public static final String GET_ALL_COUNTRIES = "SELECT * FROM " + Constants.DATABASE_TABLE;

    public static final String GET_GIVEN_COUNTRY = GET_ALL_COUNTRIES + " WHERE country_code='";

    public static final String REMOVE_COUNTRY = "DELETE FROM "+ DATABASE_TABLE +" WHERE country_code='";

    public static final String EMPTY_LIST_FRAGMENT = "EMPTY_LIST_FRAGMENT";

}
