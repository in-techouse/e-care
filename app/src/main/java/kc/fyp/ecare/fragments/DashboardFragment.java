package kc.fyp.ecare.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import kc.fyp.ecare.R;

public class DashboardFragment extends Fragment {
    public static final String TAG = "DashboardFragment";

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        return root;
    }
}