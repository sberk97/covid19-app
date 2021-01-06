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
        String mCurrent = mCountryList.get(position).getCountryName();
        holder.countryItemView.setText(mCurrent);
    }

    @Override
    public int getItemCount() {
        return mCountryList.size();
    }

    class SelectedCountryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView countryItemView;
        OnSelectedCountryListener OnSelectedCountryListener;

        public SelectedCountryViewHolder(View itemView, OnSelectedCountryListener onSelectedCountryListener) {
            super(itemView);
            countryItemView = itemView.findViewById(R.id.selected_country_name);
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
