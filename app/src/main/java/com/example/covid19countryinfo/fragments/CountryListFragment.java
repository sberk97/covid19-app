package com.example.covid19countryinfo.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Response;
import com.example.covid19countryinfo.R;
import com.example.covid19countryinfo.activities.DetailsCountryActivity;
import com.example.covid19countryinfo.activities.MainActivity;
import com.example.covid19countryinfo.adapters.SelectedCountryListAdapter;
import com.example.covid19countryinfo.misc.Constants;
import com.example.covid19countryinfo.misc.DatabaseHelper;
import com.example.covid19countryinfo.misc.DatabaseOperations;
import com.example.covid19countryinfo.misc.NoCasesException;
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
        mRecyclerView = view.findViewById(R.id.main_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new SelectedCountryListAdapter(getContext(), mSelectedCountryList, this);
        mRecyclerView.setAdapter(mAdapter);
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
                        updateGivenCountry(mSelectedCountryList.get(countryClicked));
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

    private void updateGivenCountry(Country country) {
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
        }

        new UpdateCountryTask().execute(country);
    }

    private void onDataFetchError() {
        Toast.makeText(getContext(), R.string.data_fetch_error, Toast.LENGTH_SHORT).show();
    }

    private Response.Listener<JSONObject> getUpdateCountryListener(Country country, boolean getYesterdayData) {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    boolean isNewData = country.updateObject(getYesterdayData, response, mDb);

                    if (isNewData) {
                        mAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    onDataFetchError();
                } catch (SQLException e) {
                    Toast.makeText(getContext(), R.string.update_failed, Toast.LENGTH_SHORT).show();
                } catch (NoCasesException e) {
                    country.update(true, getContext(), getUpdateCountryListener(country, true), getUpdateCountryErrorListener());
                }
            }
        };
    }

    private Response.ErrorListener getUpdateCountryErrorListener() {
        return error -> {
            error.printStackTrace();
            onDataFetchError();
        };
    }

    private class UpdateAllCountriesTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            for (Country country : mSelectedCountryList) {
                country.update(false, getContext(), getUpdateCountryListener(country, false), getUpdateCountryErrorListener());
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            onRefreshComplete();
        }

    }

    private class UpdateCountryTask extends AsyncTask<Country, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Country... params) {
            params[0].update(false, getContext(), getUpdateCountryListener(params[0], false), getUpdateCountryErrorListener());
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            onRefreshComplete();
        }

    }
}