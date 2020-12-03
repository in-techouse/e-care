package kc.fyp.ecare.fragments;

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

import kc.fyp.ecare.R;
import kc.fyp.ecare.director.Constants;
import kc.fyp.ecare.director.Helpers;
import kc.fyp.ecare.director.Session;
import kc.fyp.ecare.models.Notification;
import kc.fyp.ecare.models.User;

public class MyNotificationsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    // Create firebase database reference, to fetch my notifications from Firebase database.
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.NOTIFICATIONS_TABLE);
    private SwipeRefreshLayout swipeRefreshLayout;
    // User, to get value of current logged in user.
    private User user;
    // A list of notifications, to save the notifications data, temporarily.
    private List<Notification> data;

    public MyNotificationsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_my_notifications, container, false);

        // Find view by id, all widgets.
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        RecyclerView notifications = root.findViewById(R.id.notifications);
        // Set swipeRefreshLayout, refresh listener.
        swipeRefreshLayout.setOnRefreshListener(this);
        // Session, to get value of current logged in user.
        Session session = new Session(getActivity());
        user = session.getUser(); // this line will return you the value of current logged in user.
        data = new ArrayList<>();
        // Set recycler view properties to display the data to user.
        notifications.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Fetch my notifications from firebase database.
        loadData();

        return root;
    }

    // Fetch my notifications from firebase database.
    private void loadData() {
        // Show loading bar to user.
        swipeRefreshLayout.setRefreshing(true);

        // Get my notifications, where userId == user.getId()
        reference.orderByChild("userId").equalTo(user.getId()).addValueEventListener(new ValueEventListener() {
            // Data loading success function
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                data.clear();
                Collections.reverse(data);
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