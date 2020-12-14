package kc.thefyp.ecare.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kc.thefyp.ecare.R;
import kc.thefyp.ecare.adapters.DonationAdapter;
import kc.thefyp.ecare.director.Constants;
import kc.thefyp.ecare.director.Helpers;
import kc.thefyp.ecare.models.Announcement;
import kc.thefyp.ecare.models.Donation;

public class AllDonations extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "AllDonations";
    // Create firebase database reference, to fetch all the announcements from Firebase database.
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.DONATION_TABLE);
    // Firebase Value Event listener, used to fetched data from firebase database, along with firebase database reference.
    private ValueEventListener listener;
    // A list of donations, to save the donations data, temporarily.
    private List<Donation> data, activeList;
    private SwipeRefreshLayout swipeRefreshLayout;
    // The purpose of adapter is to show all the loaded data in the recycler view.
    private DonationAdapter donationAdapter;
    private EditText edtName, edtAddress;
    private Spinner category;
    private BottomSheetBehavior sheetBehavior;

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
        activeList = new ArrayList<>();
        // Set recycler view properties to display the data to user.
        donations.setLayoutManager(new LinearLayoutManager(AllDonations.this));
        donationAdapter = new DonationAdapter(getApplicationContext());
        donations.setAdapter(donationAdapter);

        LinearLayout layoutBottomSheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(layoutBottomSheet);
        sheetBehavior.setHideable(true);
        sheetBehavior.setPeekHeight(0);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        edtName = findViewById(R.id.edtName);
        edtAddress = findViewById(R.id.edtAddress);
        category = findViewById(R.id.category);
        AppCompatButton startSearch = findViewById(R.id.startSearch);
        AppCompatButton closeSheet = findViewById(R.id.closeSheet);

        startSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strName = edtName.getText().toString();
                String strAddress = edtAddress.getText().toString();
                String strCategory = category.getSelectedItem().toString();
                activeList.clear();
                donationAdapter.clear();
                for (Donation donation : data) {
                    if (!strCategory.equals("Select Your Category") && donation.getCategory().equals(strCategory)) {
                        activeList.add(donation);
                    } else if (strName != null && strName.length() > 0 && donation.getName() != null && donation.getName().toLowerCase().contains(strName.toLowerCase())) {
                        activeList.add(donation);
                    } else if (strAddress != null && strAddress.length() > 0 && donation.getAddress() != null && donation.getAddress().toLowerCase().contains(strAddress.toLowerCase())) {
                        activeList.add(donation);
                    }
                }
                donationAdapter.setData(activeList);
                sheetBehavior.setHideable(true);
                sheetBehavior.setPeekHeight(0);
                sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        closeSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sheetBehavior.setHideable(true);
                sheetBehavior.setPeekHeight(0);
                sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });


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
                donationAdapter.clear();
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
                activeList.addAll(data);
                donationAdapter.setData(activeList);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                break;
            }
            case R.id.action_search: {
                Log.e(TAG, "Search icon pressed");
                sheetBehavior.setHideable(false);
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                return true;
            }
        }
        return true;
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
}