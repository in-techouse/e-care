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
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.DONATION_TABLE);
    private ValueEventListener listener;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Donation> data;
    private DonationAdapter donationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_donations);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        RecyclerView donations = findViewById(R.id.donations);
        data = new ArrayList<>();
        donations.setLayoutManager(new LinearLayoutManager(AllDonations.this));
        donationAdapter = new DonationAdapter(getApplicationContext());
        donations.setAdapter(donationAdapter);
        loadData();
    }

    private void loadData() {
        swipeRefreshLayout.setRefreshing(true);

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                data.clear();
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (DataSnapshot d : snapshot.getChildren()) {
                        Donation donation = d.getValue(Donation.class);
                        if (donation != null) {
                            data.add(donation);
                        }
                    }
                }
                Collections.reverse(data);
                donationAdapter.setData(data);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefreshLayout.setRefreshing(false);
                Helpers.showError(AllDonations.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
            }
        };
        reference.addValueEventListener(listener);
    }

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