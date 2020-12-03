package kc.fyp.ecare.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kc.fyp.ecare.R;
import kc.fyp.ecare.adapters.AnnouncementAdapter;
import kc.fyp.ecare.director.Constants;
import kc.fyp.ecare.director.Helpers;
import kc.fyp.ecare.models.Announcement;
import kc.fyp.ecare.models.Donation;

public class AllAnnouncements extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    // Create firebase database reference, to fetch all the announcements from Firebase database.
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.ANNOUNCEMENT_TABLE);
    // Firebase Value Event listener, used to fetched data from firebase database, along with firebase database reference.
    private ValueEventListener listener;
    // A list of announcements, to save the announcements data, temporarily.
    private List<Announcement> data;
    private SwipeRefreshLayout swipeRefreshLayout;
    // The purpose of adapter is to show all the loaded data in the recycler view.
    private AnnouncementAdapter announcementAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_announcements);

        // Find view by id, all widgets.
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        RecyclerView announcements = findViewById(R.id.announcements);
        // Set swipeRefreshLayout, refresh listener.
        swipeRefreshLayout.setOnRefreshListener(this);
        data = new ArrayList<>();
        // Set recycler view properties to display the data to user.
        announcements.setLayoutManager(new LinearLayoutManager(AllAnnouncements.this));
        announcementAdapter = new AnnouncementAdapter(getApplicationContext());
        announcements.setAdapter(announcementAdapter);
        // Fetch all announcements from firebase database.
        loadData();
    }

    // Fetch all announcements from firebase database.
    private void loadData() {
        // Show loading bar
        swipeRefreshLayout.setRefreshing(true);

        listener = new ValueEventListener() {
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
                // Show an error to user.
                Helpers.showError(AllAnnouncements.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
            }
        };
        reference.addValueEventListener(listener);
    }

    // SwipeRefreshLayout, refresh listener.
    @Override
    public void onRefresh() {
        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            reference.removeEventListener(listener);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
        }
        return true;
    }
}