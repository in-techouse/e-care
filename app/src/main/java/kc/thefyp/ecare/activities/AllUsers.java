package kc.thefyp.ecare.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kc.thefyp.ecare.R;
import kc.thefyp.ecare.adapters.UserAdapter;
import kc.thefyp.ecare.director.Constants;
import kc.thefyp.ecare.director.Helpers;
import kc.thefyp.ecare.director.Session;
import kc.thefyp.ecare.models.User;

public class AllUsers extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private final static String TAG = "AllUsers";
    // Create firebase database reference, to fetch all the announcements from Firebase database.
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.USER_TABLE);
    // Firebase Value Event listener, used to fetched data from firebase database, along with firebase database reference.
    private ValueEventListener listener;
    // A list of users, to save the users data, temporarily.
    private List<User> data, activeList;
    private SwipeRefreshLayout swipeRefreshLayout;
    // The purpose of adapter is to show all the loaded data in the recycler view.
    private UserAdapter userAdapter;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_users);

        // Find view by id, all widgets.
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        RecyclerView users = findViewById(R.id.users);
        // Set swipeRefreshLayout, refresh listener.
        swipeRefreshLayout.setOnRefreshListener(this);
        data = new ArrayList<>();
        activeList = new ArrayList<>();
        // Set recycler view properties to display the data to user.
        users.setLayoutManager(new LinearLayoutManager(AllUsers.this));
        userAdapter = new UserAdapter(getApplicationContext());
        users.setAdapter(userAdapter);
        Session session = new Session(getApplicationContext());
        currentUser = session.getUser();
        // Fetch all users from firebase database.
        loadData();
    }

    // Fetch all users from firebase database.
    private void loadData() {
        // Show loading bar
        swipeRefreshLayout.setRefreshing(true);

        listener = new ValueEventListener() {
            // Data is loaded successfully.
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the data list.
                data.clear();
                // Insert all the loaded Users, to data list.
                if (snapshot.exists() && snapshot.hasChildren()) {
                    for (DataSnapshot d : snapshot.getChildren()) {
                        User user = d.getValue(User.class);
                        if (user != null && !user.getId().equals(currentUser.getId())) {
                            data.add(user);
                        }
                    }
                }
                // Reverse the data list, so that the latest user should display on top.
                Collections.reverse(data);
                activeList.addAll(data);
                userAdapter.setData(activeList);
                // Hide the loading bar.
                swipeRefreshLayout.setRefreshing(false);
            }

            // Data is loading failed.
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Hide the loading bar.
                swipeRefreshLayout.setRefreshing(false);
                // Show an error to user.
                Helpers.showError(AllUsers.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
            }
        };
        reference.orderByChild("rating").addValueEventListener(listener);
    }

    // SwipeRefreshLayout, refresh listener.
    @Override
    public void onRefresh() {
        loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_search, menu);

        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();

        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.e(TAG, "Query is: " + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.e(TAG, "New Text is: " + newText);
                swipeRefreshLayout.setRefreshing(true);
                userAdapter.clear();
                activeList.clear();
                for (User user : data) {
                    if (user.getName().toLowerCase().contains(newText.toLowerCase())) {
                        activeList.add(user);
                    }
                }
                userAdapter.setData(activeList);
                swipeRefreshLayout.setRefreshing(false);
                return false;
            }
        });
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