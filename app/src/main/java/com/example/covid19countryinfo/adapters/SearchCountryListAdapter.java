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
import com.example.covid19countryinfo.models.SearchListCountry;

import java.util.ArrayList;
import java.util.List;

public class SearchCountryListAdapter extends
        RecyclerView.Adapter<SearchCountryListAdapter.SearchCountryViewHolder> implements Filterable {

    private List<SearchListCountry> mCountryList;
    private List<SearchListCountry> mCountryListFull;
    private LayoutInflater mInflater;
    private OnSearchCountryListener mOnSearchCountryListener;

    public SearchCountryListAdapter(Context context, List<SearchListCountry> countryList, OnSearchCountryListener onSearchCountryListener) {
        mInflater = LayoutInflater.from(context);
        this.mCountryList = countryList;
        this.mCountryListFull = new ArrayList<>(countryList);
        this.mOnSearchCountryListener = onSearchCountryListener;
    }

    @NonNull
    @Override
    public SearchCountryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.search_country_list_item, parent, false);
        return new SearchCountryViewHolder(mItemView, mOnSearchCountryListener);
    }

    @Override
    public void onBindViewHolder(SearchCountryViewHolder holder, int position) {
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
            List<SearchListCountry> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(mCountryListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (SearchListCountry item : mCountryListFull) {
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

    class SearchCountryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView countryItemView;
        OnSearchCountryListener onSearchCountryListener;

        public SearchCountryViewHolder(View itemView, OnSearchCountryListener onSearchCountryListener) {
            super(itemView);
            countryItemView = itemView.findViewById(R.id.search_country_name);
            this.onSearchCountryListener = onSearchCountryListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onSearchCountryListener.onCountryClick(getAdapterPosition());
        }

    }

    public interface OnSearchCountryListener {
        void onCountryClick(int position);
    }
}
