package com.example.covid19countryinfo.models;

public class SelectedListCountry {
    private String countryName;
    private String countryCode;
    private int todayCases;

    public SelectedListCountry(String countryName, String countryCode, int todayCases, int todayDeaths, int todayRecovered, String lastUpdateDate) {
        this.countryName = countryName;
        this.countryCode = countryCode;
        this.todayCases = todayCases;
        this.todayDeaths = todayDeaths;
        this.todayRecovered = todayRecovered;
        this.lastUpdateDate = lastUpdateDate;
    }

    public int getTodayCases() {
        return todayCases;
    }

    public void setTodayCases(int todayCases) {
        this.todayCases = todayCases;
    }

    public int getTodayDeaths() {
        return todayDeaths;
    }

    public void setTodayDeaths(int todayDeaths) {
        this.todayDeaths = todayDeaths;
    }

    public int getTodayRecovered() {
        return todayRecovered;
    }

    public void setTodayRecovered(int todayRecovered) {
        this.todayRecovered = todayRecovered;
    }

    public String getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(String lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    private int todayDeaths;
    private int todayRecovered;
    private String lastUpdateDate;

    public SelectedListCountry(String countryName, String countryCode) {
        this.countryName = countryName;
        this.countryCode = countryCode;
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
