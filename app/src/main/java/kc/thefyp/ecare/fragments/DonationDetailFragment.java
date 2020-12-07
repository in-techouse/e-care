package kc.thefyp.ecare.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import kc.thefyp.ecare.R;
import kc.thefyp.ecare.models.Request;

public class DonationDetailFragment extends Fragment {
    private static final String TAG = "DonationDetailFragment";
    private Request request;
    private LinearLayout mainRoot;

    public DonationDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_donation_detail, container, false);
        mainRoot = root.findViewById(R.id.mainRoot);
        mainRoot.setVisibility(View.GONE);
        return root;
    }

    public void setRequest(Request request) {
        this.request = request;
        Log.e(TAG, "Request received");
        Log.e(TAG, "Request id is: " + this.request.getId());
//        mainRoot.setVisibility(View.VISIBLE);
    }
}