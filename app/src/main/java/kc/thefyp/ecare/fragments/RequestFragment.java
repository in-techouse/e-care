package kc.thefyp.ecare.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kc.thefyp.ecare.R;
import kc.thefyp.ecare.adapters.RequestAdapter;
import kc.thefyp.ecare.director.Constants;
import kc.thefyp.ecare.director.Helpers;
import kc.thefyp.ecare.director.Session;
import kc.thefyp.ecare.models.Request;
import kc.thefyp.ecare.models.User;

public class RequestFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    // Create firebase database reference, to fetch my requests from Firebase database.
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.REQUEST_TABLE);
    private SwipeRefreshLayout swipeRefreshLayout;
    // User, to get value of current logged in user.
    private User user;
    // A list of requests, to save the requests data, temporarily.
    private List<Request> data;
    // The purpose of adapter is to show all the loaded data in the recycler view.
    private RequestAdapter requestAdapter;

    public RequestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_request, container, false);

        // Find view by id, all widgets.
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        RecyclerView requests = root.findViewById(R.id.requests);
        // Set swipeRefreshLayout, refresh listener.
        swipeRefreshLayout.setOnRefreshListener(this);
        // Session, to get value of current logged in user.
        Session session = new Session(getActivity());
        user = session.getUser(); // this line will return you the value of current logged in user.
        data = new ArrayList<>();
        // Set recycler view properties to display the data to user.
        requests.setLayoutManager(new LinearLayoutManager(getActivity()));
        requestAdapter = new RequestAdapter(getActivity());
        requests.setAdapter(requestAdapter);
        // Fetch my requests from firebase database.
        loadData();

        return root;
    }

    // Fetch my notifications from firebase database.
    private void loadData() {
        // Show loading bar to user.
        swipeRefreshLayout.setRefreshing(true);

        // Get my notifications, where userId == user.getId()
        reference.orderByChild("toUser").equalTo(user.getId()).addValueEventListener(new ValueEventListener() {
            // Data loading success function
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the data list.
                data.clear();
                // Insert all the loaded requests, to data list.
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (DataSnapshot d : snapshot.getChildren()) {
                        Request request = d.getValue(Request.class);
                        if (request != null) {
                            data.add(request);
                        }
                    }
                }
                // Reverse the data list, so that the latest requests should display on top.
                Collections.reverse(data);
                requestAdapter.setData(data);
                // Hide the loading bar.
                swipeRefreshLayout.setRefreshing(false);
            }

            // Data loading failed function
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Hide loading bar
                swipeRefreshLayout.setRefreshing(true);
                // Show error to user, that data is not loaded.
                Helpers.showError(getActivity(), Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
            }
        });
    }

    @Override
    public void onRefresh() {
        loadData();
    }
}