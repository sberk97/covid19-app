package com.example.covid19countryinfo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

public class CountryListAdapter extends
        RecyclerView.Adapter<CountryListAdapter.CountryViewHolder>  {

    private final LinkedList<String> mCountryList;
    private LayoutInflater mInflater;
    private OnCountryListener mOnCountryListener;

    public CountryListAdapter(Context context, LinkedList<String> countryList, OnCountryListener onCountryListener) {
        mInflater = LayoutInflater.from(context);
        this.mCountryList = countryList;
        this.mOnCountryListener = onCountryListener;
    }

    @NonNull
    @Override
    public CountryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.countrylist_item, parent, false);
        return new CountryViewHolder(mItemView, mOnCountryListener);
    }

    @Override
    public void onBindViewHolder(CountryViewHolder holder, int position) {
        String mCurrent = mCountryList.get(position);
        holder.countryItemView.setText(mCurrent);
    }

    @Override
    public int getItemCount() {
        return mCountryList.size();
    }

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
