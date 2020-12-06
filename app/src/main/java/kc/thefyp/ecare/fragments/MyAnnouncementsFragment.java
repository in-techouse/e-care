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
import kc.thefyp.ecare.adapters.AnnouncementAdapter;
import kc.thefyp.ecare.director.Constants;
import kc.thefyp.ecare.director.Helpers;
import kc.thefyp.ecare.director.Session;
import kc.thefyp.ecare.models.Announcement;
import kc.thefyp.ecare.models.User;

public class MyAnnouncementsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    // Create firebase database reference, to fetch my announcements from Firebase database.
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.ANNOUNCEMENT_TABLE);
    private SwipeRefreshLayout swipeRefreshLayout;
    // User, to get value of current logged in user.
    private User user;
    // A list of announcements, to save the announcements data, temporarily.
    private List<Announcement> data;
    // The purpose of adapter is to show all the loaded data in the recycler view.
    private AnnouncementAdapter announcementAdapter;

    public MyAnnouncementsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_my_announcements, container, false);

        // Find view by id, all widgets.
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        RecyclerView announcements = root.findViewById(R.id.announcements);
        // Set swipeRefreshLayout, refresh listener.
        swipeRefreshLayout.setOnRefreshListener(this);
        // Session, to get value of current logged in user.
        Session session = new Session(getActivity());
        user = session.getUser(); // this line will return you the value of current logged in user.
        data = new ArrayList<>();
        // Set recycler view properties to display the data to user.
        announcements.setLayoutManager(new LinearLayoutManager(getActivity()));
        announcementAdapter = new AnnouncementAdapter(getActivity());
        announcements.setAdapter(announcementAdapter);
        // Fetch my announcements from firebase database.
        loadData();

        return root;
    }

    // Fetch my announcements from firebase database.
    private void loadData() {
        // Show loading bar
        swipeRefreshLayout.setRefreshing(true);
        // Get my announcements, where userId == user.getId()
        reference.orderByChild("userId").equalTo(user.getId()).addValueEventListener(new ValueEventListener() {
            // Data is loaded successfully.
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the data list.
                data.clear();
                // Insert all the loaded announcements, to data list.
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (DataSnapshot d : snapshot.getChildren()) {
                        Announcement announcement = d.getValue(Announcement.class);
                        if (announcement != null) {
                            data.add(announcement);
                        }
                    }
                }
                // Reverse the data list, so that the latest announcement should display on top.
                Collections.reverse(data);
                announcementAdapter.setData(data);
                // Hide the loading bar.
                swipeRefreshLayout.setRefreshing(false);
            }

            // Data is loading failed.
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Hide the loading bar.
                swipeRefreshLayout.setRefreshing(false);
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