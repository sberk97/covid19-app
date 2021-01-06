package com.example.covid19countryinfo.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.covid19countryinfo.R;

public class EmptyListFragment extends Fragment {

    public EmptyListFragment() {
        // Required empty public constructor
    }

    public static EmptyListFragment newInstance() {
        return new EmptyListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        final View rootView = inflater.inflate(R.layout.fragment_empty_list, container, false);

        // Return the View for the fragment's UI.
        return rootView;
    }
}