package com.example.covid19countryinfo.fragments;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.covid19countryinfo.R;
import com.example.covid19countryinfo.activities.MainActivity;
import com.example.covid19countryinfo.adapters.SelectedCountryListAdapter;
import com.example.covid19countryinfo.misc.Constants;
import com.example.covid19countryinfo.misc.DatabaseHelper;
import com.example.covid19countryinfo.misc.Helper;
import com.example.covid19countryinfo.misc.RequestQueueSingleton;
import com.example.covid19countryinfo.models.Country;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CountryListFragment extends Fragment implements SelectedCountryListAdapter.OnSelectedCountryListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private SQLiteDatabase mDb;

    private List<Country> mSelectedCountryList = new ArrayList<>();

    public List<Country> getSelectedCountryList() {
        return mSelectedCountryList;
    }

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private RecyclerView mRecyclerView;

    private SelectedCountryListAdapter mAdapter;

    public SelectedCountryListAdapter getAdapter() {
        return mAdapter;
    }

    public CountryListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentCountryList.
     */
    // TODO: Rename and change types and number of parameters
    public static CountryListFragment newInstance(String param1, String param2) {
        CountryListFragment fragment = new CountryListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
        DatabaseHelper sqLiteHelper = DatabaseHelper.getInstance(getContext());
        mDb = sqLiteHelper.getWritableDatabase();

        setUpCountryList();
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_country_list, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_to_refresh);

//        mSwipeRefreshLayout.setColorScheme(
//                R.color.swipe_color_1, R.color.swipe_color_2,
//                R.color.swipe_color_3, R.color.swipe_color_4);

        setUpRecyclerView(view);

        return view;
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
        mSelectedCountryList = fetchCountries(Constants.GET_ALL_COUNTRIES);
    }

    @Override
    public void onCountryClick(int position) {
        // move to country activity
    }

    private void updateCountryInList(String countryCode, int countryListIndex) {
        String sql = Constants.GET_GIVEN_COUNTRY + countryCode + "';";
        List<Country> newCountryData = fetchCountries(sql);
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

    private List<Country> fetchCountries(String sql) {
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
            Toast.makeText(getContext(), R.string.error_country_retrieve, Toast.LENGTH_SHORT).show();
        }

        return retrievedCountries;
    }

    public void addCountryToList(String countryCode) {
        mSelectedCountryList.add(fetchCountries(Constants.GET_GIVEN_COUNTRY + countryCode + "';").get(0));
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

    private void updateAllCountries() {
        String countryCodes = Helper.getCountryCodesString(mSelectedCountryList);
        int i = 0;
        for (Country country : mSelectedCountryList) {
            getDataFromApiAndUpdate(country, i, false);
            i++;
        }
    }

    private void updateGivenCountry(int countryClicked) {
        Country country = mSelectedCountryList.get(countryClicked);
        getDataFromApiAndUpdate(country, countryClicked, false);
    }

    public void getDataFromApiAndUpdate(Country country, int countryListIndex, boolean getYesterdayData) {
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

}