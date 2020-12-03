package kc.fyp.ecare.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import kc.fyp.ecare.R;
import kc.fyp.ecare.activities.AllAnnouncements;
import kc.fyp.ecare.activities.AllDonations;

public class DashboardFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = "DashboardFragment";

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Find view by Id, all widgets
        RelativeLayout allAnnouncements = root.findViewById(R.id.allAnnouncements);
        RelativeLayout allProducts = root.findViewById(R.id.allDonations);
        // Set click listener on relevant widgets
        allAnnouncements.setOnClickListener(this);
        allProducts.setOnClickListener(this);
        return root;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            // Clicked on All Announcements
            case R.id.allAnnouncements: {
                // Start all announcement activity, to display all the announcements.
                Intent it = new Intent(getActivity(), AllAnnouncements.class);
                startActivity(it);
                break;
            }
            // Clicked on All Donations
            case R.id.allDonations: {
                // Start all donations activity, to display all the donations.
                Intent it = new Intent(getActivity(), AllDonations.class);
                startActivity(it);
                break;
            }
        }
    }
}