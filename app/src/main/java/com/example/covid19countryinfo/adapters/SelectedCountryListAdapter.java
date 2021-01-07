package com.example.covid19countryinfo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid19countryinfo.R;
import com.example.covid19countryinfo.models.SelectedListCountry;

import java.util.List;

public class SelectedCountryListAdapter extends
        RecyclerView.Adapter<SelectedCountryListAdapter.SelectedCountryViewHolder> {

    private List<SelectedListCountry> mCountryList;
    private LayoutInflater mInflater;
    private OnSelectedCountryListener mOnSelectedCountryListener;

    public SelectedCountryListAdapter(Context context, List<SelectedListCountry> countryList, OnSelectedCountryListener onSelectedCountryListener) {
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
        int countryCases = mCountryList.get(position).getTodayCases();
        int countryDeaths = mCountryList.get(position).getTodayDeaths();
        int countryRecovered = mCountryList.get(position).getTodayRecovered();
        String countryLastUpdate = mCountryList.get(position).getLastUpdateDate();

        int commaInCountryName = countryName.indexOf(',');
        if (commaInCountryName != -1) {
            countryName = countryName.substring(0, commaInCountryName);
        }

        holder.countryNameView.setText(countryName);
        holder.countryCasesView.setText(String.valueOf(countryCases));
        holder.countryDeathsView.setText(String.valueOf(countryDeaths));
        holder.countryRecoveredView.setText(String.valueOf(countryRecovered));
        holder.countryLastUpdateView.setText(countryLastUpdate);

        if (countryCases == 0 && countryDeaths == 0 && countryRecovered == 0) {
            holder.countryNoReportView.setVisibility(View.VISIBLE);
        }
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
        public final TextView countryNoReportView;

        OnSelectedCountryListener OnSelectedCountryListener;

        public SelectedCountryViewHolder(View itemView, OnSelectedCountryListener onSelectedCountryListener) {
            super(itemView);
            countryNameView = itemView.findViewById(R.id.selected_country_name);
            countryCasesView = itemView.findViewById(R.id.selected_country_today_cases);
            countryDeathsView = itemView.findViewById(R.id.selected_country_today_deaths);
            countryRecoveredView = itemView.findViewById(R.id.selected_country_today_recovered);
            countryLastUpdateView = itemView.findViewById(R.id.selected_country_last_update);
            countryNoReportView = itemView.findViewById(R.id.selected_country_no_reports);

            this.OnSelectedCountryListener = onSelectedCountryListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            OnSelectedCountryListener.onCountryClick(getAdapterPosition());
        }

    }

    public interface OnSelectedCountryListener {
        void onCountryClick(int position);
    }
}
