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
import kc.fyp.ecare.adapters.DonationAdapter;
import kc.fyp.ecare.director.Constants;
import kc.fyp.ecare.director.Helpers;
import kc.fyp.ecare.models.Donation;

public class AllDonations extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    // Create firebase database reference, to fetch all the announcements from Firebase database.
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.DONATION_TABLE);
    // Firebase Value Event listener, used to fetched data from firebase database, along with firebase database reference.
    private ValueEventListener listener;
    // A list of donations, to save the donations data, temporarily.
    private List<Donation> data;
    private SwipeRefreshLayout swipeRefreshLayout;
    // The purpose of adapter is to show all the loaded data in the recycler view.
    private DonationAdapter donationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_donations);

        // Find view by id, all widgets.
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        RecyclerView donations = findViewById(R.id.donations);
        // Set swipeRefreshLayout, refresh listener.
        swipeRefreshLayout.setOnRefreshListener(this);
        data = new ArrayList<>();
        // Set recycler view properties to display the data to user.
        donations.setLayoutManager(new LinearLayoutManager(AllDonations.this));
        donationAdapter = new DonationAdapter(getApplicationContext());
        donations.setAdapter(donationAdapter);
        // Fetch all donations from firebase database.
        loadData();
    }

    // Fetch all donations from firebase database.
    private void loadData() {
        // Show loading bar
        swipeRefreshLayout.setRefreshing(true);

        listener = new ValueEventListener() {
            // Data is loaded successfully.
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the data list.
                data.clear();
                // Insert all the loaded donations, to data list.
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (DataSnapshot d : snapshot.getChildren()) {
                        Donation donation = d.getValue(Donation.class);
                        if (donation != null && !donation.isDonated() && donation.getDonatedTo().length() < 1) {
                            data.add(donation);
                        }
                    }
                }
                // Reverse the data list, so that the latest donation should display on top.
                Collections.reverse(data);
                donationAdapter.setData(data);
                // Hide the loading bar.
                swipeRefreshLayout.setRefreshing(false);
            }

            // Data is loading failed.
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Hide the loading bar.
                swipeRefreshLayout.setRefreshing(false);
                // Show an error to user.
                Helpers.showError(AllDonations.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
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