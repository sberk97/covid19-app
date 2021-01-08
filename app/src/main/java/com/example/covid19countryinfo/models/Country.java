package com.example.covid19countryinfo.models;

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
}
