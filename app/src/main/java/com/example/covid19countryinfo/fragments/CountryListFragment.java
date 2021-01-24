package com.example.covid19countryinfo.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.covid19countryinfo.R;
import com.example.covid19countryinfo.activities.DetailsCountryActivity;
import com.example.covid19countryinfo.activities.MainActivity;
import com.example.covid19countryinfo.activities.SearchCountryActivity;
import com.example.covid19countryinfo.adapters.SelectedCountryListAdapter;
import com.example.covid19countryinfo.misc.Constants;
import com.example.covid19countryinfo.misc.DatabaseHelper;
import com.example.covid19countryinfo.misc.DatabaseOperations;
import com.example.covid19countryinfo.misc.Helper;
import com.example.covid19countryinfo.misc.RequestQueueSingleton;
import com.example.covid19countryinfo.models.Country;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CountryListFragment extends Fragment implements SelectedCountryListAdapter.OnSelectedCountryListener {

    private SQLiteDatabase mDb;

    private List<Country> mSelectedCountryList = new ArrayList<>();

    public List<Country> getSelectedCountryList() {
        return mSelectedCountryList;
    }

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;

    private SelectedCountryListAdapter mAdapter;

    public CountryListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_country_list, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_to_refresh);

        DatabaseHelper sqLiteHelper = DatabaseHelper.getInstance(getContext());
        mDb = sqLiteHelper.getWritableDatabase();

        setUpCountryList();

        mSwipeRefreshLayout.setColorSchemeResources(R.color.light_green);

        setUpRecyclerView(view);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initiateRefresh();
            }
        });
    }

    private void onRefreshComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void initiateRefresh() {
        new UpdateAllCountriesTask().execute();
    }

    private void setUpRecyclerView(View view) {
        // Get a handle to the RecyclerView.
        mRecyclerView = view.findViewById(R.id.main_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        // Create an adapter and supply the data to be displayed.
        mAdapter = new SelectedCountryListAdapter(getContext(), mSelectedCountryList, this);
        // Connect the adapter with the RecyclerView.
        mRecyclerView.setAdapter(mAdapter);
        // Give the RecyclerView a default layout manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setUpCountryList() {
        mSelectedCountryList = DatabaseOperations.fetchCountries(Constants.GET_ALL_COUNTRIES, getContext(), mDb);
    }

    @Override
    public void onCountryClick(int position) {
        Intent intent = new Intent(getActivity(), DetailsCountryActivity.class);
        intent.putExtra(Constants.EXTRA_CLICKED_COUNTRY_CODE, mSelectedCountryList.get(position).getCountryCode());
        intent.putExtra(Constants.EXTRA_CLICKED_COUNTRY_NAME, mSelectedCountryList.get(position).getCountryName());
        startActivity(intent);
    }

    private void updateCountryInList(String countryCode, int countryListIndex) {
        String sql = Constants.GET_GIVEN_COUNTRY + countryCode + "';";
        List<Country> newCountryData = DatabaseOperations.fetchCountries(sql, getContext(), mDb);
        mSelectedCountryList.set(countryListIndex, newCountryData.get(0));
    }

    private void removeCountry(int countryClicked) {
        String countryCode = mSelectedCountryList.get(countryClicked).getCountryCode();
        try {
            mDb.execSQL(Constants.REMOVE_COUNTRY + countryCode + "'");
            mSelectedCountryList.remove(countryClicked);
            mAdapter.notifyDataSetChanged();
            if (mSelectedCountryList.isEmpty()) {
                MainActivity activity = (MainActivity) getActivity();
                activity.displayEmptyListFragment();
            }
        } catch (SQLException e) {
            Toast.makeText(getContext(), R.string.error_country_remove, Toast.LENGTH_SHORT).show();
        }
    }

    public void addCountryToList(String countryCode) {
        mSelectedCountryList.add(DatabaseOperations.fetchCountries(Constants.GET_GIVEN_COUNTRY + countryCode + "';", getContext(), mDb).get(0));
        mAdapter.notifyDataSetChanged();
    }

    public void showCountryCardSettings(View view) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.getMenuInflater().inflate(R.menu.selected_country_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(getOnMenuItemClickListener(view));
        popup.show();
    }

    @SuppressLint("NonConstantResourceId")
    private PopupMenu.OnMenuItemClickListener getOnMenuItemClickListener(View view) {
        return new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                CardView cView = (CardView) ((ViewGroup) view.getParent()).getParent();
                int countryClicked = mRecyclerView.getChildAdapterPosition(cView);
                switch (item.getItemId()) {
                    case R.id.action_remove:
                        removeCountry(countryClicked);
                        return true;
                    case R.id.action_update_item:
                        updateGivenCountry(countryClicked);
                        return true;
                }
                return true;
            }
        };
    }

    public void updateAllCountries() {
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
        }

        initiateRefresh();
    }

    private void updateGivenCountry(int countryClicked) {
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
        }

        new UpdateCountryTask().execute(countryClicked);
    }

    private void getDataFromApiAndUpdate(Country country, int countryListIndex, boolean getYesterdayData) {
        String url = Constants.COUNTRY_DATA_API + country.getCountryCode();
        if (getYesterdayData) {
            url += "?yesterday=true";
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                StringBuilder sb = getUpdateSQL(country, countryListIndex, getYesterdayData, response);
                if (sb == null) return;

                mDb.execSQL(sb.toString());
                updateCountryInList(country.getCountryCode(), countryListIndex);
                mAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                e.printStackTrace();
                onDataFetchError();
            } catch (SQLException e) {
                Toast.makeText(getContext(), R.string.update_failed, Toast.LENGTH_SHORT).show();
            }
        }, error -> {
            error.printStackTrace();
            onDataFetchError();
        });
        RequestQueueSingleton.getInstance(getContext()).addToRequestQueue(jsonObjectRequest);
    }

    private StringBuilder getUpdateSQL(Country country, int countryListIndex, boolean getYesterdayData, JSONObject response) throws JSONException {
        int latestCases = response.getInt("todayCases");
        int latestDeaths = response.getInt("todayDeaths");
        int latestRecovered = response.getInt("todayRecovered");
        long epochDate = response.getLong("updated");

        boolean noNewCases = latestCases == 0 && latestDeaths == 0 && latestRecovered == 0;
        boolean oldDataNoNewCases = country.getLatestCases() == 0 && country.getLatestDeaths() == 0 && country.getLatestRecovered() == 0;
        boolean dataIsTheSame = country.getLatestCases() == latestCases && country.getLatestDeaths() == latestDeaths && country.getLatestRecovered() == latestRecovered && country.getLastUpdateDate().equals(Helper.formatDate(epochDate));
        StringBuilder sb = new StringBuilder();

        if (dataIsTheSame) {
            return null;
        }
        // If today and yesterday no new cases
        else if (noNewCases && oldDataNoNewCases) {
            if (getYesterdayData) {
                epochDate -= 86400000;
                country.setLastUpdateDate(Helper.formatDate(epochDate));
                sb.append(Constants.UPDATE_COUNTRY)
                        .append("date='")
                        .append(Helper.formatDate(epochDate))
                        .append("' WHERE country_code='")
                        .append(country.getCountryCode()).append("';");
            } else {
                getDataFromApiAndUpdate(country, countryListIndex, true);
                return null;
            }
        }
        // Old data has cases, new doesn't and its not yesterday data then try getting yesterday data
        else if (noNewCases && !getYesterdayData) {
            getDataFromApiAndUpdate(country, countryListIndex, true);
            return null;
        }
        // New data has cases
        else {
            if (getYesterdayData) {
                epochDate -= 86400000;
            }

            sb.append(Constants.UPDATE_COUNTRY)
                    .append("latest_cases=")
                    .append(latestCases)
                    .append(", latest_deaths=")
                    .append(latestDeaths)
                    .append(", latest_recovered=")
                    .append(latestRecovered)
                    .append(", date='")
                    .append(Helper.formatDate(epochDate))
                    .append("' WHERE country_code='")
                    .append(country.getCountryCode()).append("';");
        }
        return sb;
    }

    private void onDataFetchError() {
        Toast.makeText(getContext(), R.string.data_fetch_error, Toast.LENGTH_SHORT).show();
    }

    private class UpdateAllCountriesTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            int i = 0;
            for (Country country : mSelectedCountryList) {
                getDataFromApiAndUpdate(country, i, false);
                i++;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            onRefreshComplete();
        }

    }

    private class UpdateCountryTask extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... params) {
            Country country = mSelectedCountryList.get(params[0]);
            getDataFromApiAndUpdate(country, params[0], false);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            onRefreshComplete();
        }

    }
}