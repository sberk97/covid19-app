package com.example.covid19countryinfo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.covid19countryinfo.R;
import com.example.covid19countryinfo.models.SearchableCountry;

import java.util.ArrayList;
import java.util.List;

public class CountryListAdapter extends
        RecyclerView.Adapter<CountryListAdapter.CountryViewHolder> implements Filterable {

    private List<SearchableCountry> mCountryList;
    private List<SearchableCountry> mCountryListFull;
    private LayoutInflater mInflater;
    private OnCountryListener mOnCountryListener;

    public CountryListAdapter(Context context, List<SearchableCountry> countryList, OnCountryListener onCountryListener) {
        mInflater = LayoutInflater.from(context);
        this.mCountryList = countryList;
        this.mCountryListFull = new ArrayList<>(countryList);
        this.mOnCountryListener = onCountryListener;
    }

    @NonNull
    @Override
    public CountryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.countrylist_item, parent, false);
        return new CountryViewHolder(mItemView, mOnCountryListener);
    }

    @Override
    public void onBindViewHolder(CountryViewHolder holder, int position) {
        String mCurrent = mCountryList.get(position).getCountryName();
        holder.countryItemView.setText(mCurrent);
    }

    @Override
    public int getItemCount() {
        return mCountryList.size();
    }

    @Override
    public Filter getFilter() {
        return countryFilter;
    }

    private Filter countryFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<SearchableCountry> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(mCountryListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (SearchableCountry item : mCountryListFull) {
                    if (item.getCountryName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mCountryList.clear();
            mCountryList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    class CountryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView countryItemView;
        OnCountryListener onCountryListener;

        public CountryViewHolder(View itemView, OnCountryListener onCountryListener) {
            super(itemView);
            countryItemView = itemView.findViewById(R.id.country);
            this.onCountryListener = onCountryListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onCountryListener.onCountryClick(getAdapterPosition());
        }

    }

    public interface OnCountryListener {
        void onCountryClick(int position);
    }
}
