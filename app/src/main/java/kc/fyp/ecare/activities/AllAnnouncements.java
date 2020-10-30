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
import java.util.List;

import kc.fyp.ecare.R;
import kc.fyp.ecare.director.Constants;
import kc.fyp.ecare.models.Announcement;
import kc.fyp.ecare.models.Donation;

public class AllAnnouncements extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.ANNOUNCEMENT_TABLE);
    private ValueEventListener listener;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView announcements;
    private List<Announcement> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_announcements);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        announcements = findViewById(R.id.announcements);
        data = new ArrayList<>();
        announcements.setLayoutManager(new LinearLayoutManager(AllAnnouncements.this));
        loadData();
    }

    private void loadData() {
        swipeRefreshLayout.setRefreshing(true);

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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