package com.example.covid19countryinfo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid19countryinfo.R;
import com.example.covid19countryinfo.misc.Helper;
import com.example.covid19countryinfo.models.Country;

import java.util.List;

public class SelectedCountryListAdapter extends
        RecyclerView.Adapter<SelectedCountryListAdapter.SelectedCountryViewHolder> {

    private List<Country> mCountryList;
    private LayoutInflater mInflater;
    private OnSelectedCountryListener mOnSelectedCountryListener;

    public SelectedCountryListAdapter(Context context, List<Country> countryList, OnSelectedCountryListener onSelectedCountryListener) {
        mInflater = LayoutInflater.from(context);
        this.mCountryList = countryList;
        this.mOnSelectedCountryListener = onSelectedCountryListener;
    }

    @NonNull
    @Override
    public SelectedCountryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.selected_country_list_item, parent, false);
        return new SelectedCountryViewHolder(mItemView, mOnSelectedCountryListener);
    }

    @Override
    public void onBindViewHolder(SelectedCountryViewHolder holder, int position) {
        String countryName = mCountryList.get(position).getCountryName();
        int countryCases = mCountryList.get(position).getLatestCases();
        int countryDeaths = mCountryList.get(position).getLatestDeaths();
        int countryRecovered = mCountryList.get(position).getLatestRecovered();
        String countryLastUpdate = mCountryList.get(position).getLastUpdateDate();

        countryName = Helper.shortenCountryName(countryName);

        holder.countryNameView.setText(countryName);
        holder.countryCasesView.setText(String.valueOf(countryCases));
        holder.countryDeathsView.setText(String.valueOf(countryDeaths));
        holder.countryRecoveredView.setText(String.valueOf(countryRecovered));
        holder.countryLastUpdateView.setText(countryLastUpdate);
    }

    @Override
    public int getItemCount() {
        return mCountryList.size();
    }

    class SelectedCountryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView countryNameView;
        public final TextView countryCasesView;
        public final TextView countryDeathsView;
        public final TextView countryRecoveredView;
        public final TextView countryLastUpdateView;

        OnSelectedCountryListener onSelectedCountryListener;

        public SelectedCountryViewHolder(View itemView, OnSelectedCountryListener onSelectedCountryListener) {
            super(itemView);
            countryNameView = itemView.findViewById(R.id.selected_country_name);
            countryCasesView = itemView.findViewById(R.id.selected_country_latest_cases);
            countryDeathsView = itemView.findViewById(R.id.selected_country_latest_deaths);
            countryRecoveredView = itemView.findViewById(R.id.selected_country_latest_recovered);
            countryLastUpdateView = itemView.findViewById(R.id.selected_country_last_update);

            this.onSelectedCountryListener = onSelectedCountryListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onSelectedCountryListener.onCountryClick(getAdapterPosition());
        }

    }

    public interface OnSelectedCountryListener {
        void onCountryClick(int position);
    }
}
