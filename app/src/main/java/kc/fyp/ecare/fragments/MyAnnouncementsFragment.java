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
import java.util.List;

import kc.fyp.ecare.R;
import kc.fyp.ecare.director.Constants;
import kc.fyp.ecare.director.Session;
import kc.fyp.ecare.models.Announcement;
import kc.fyp.ecare.models.User;

public class MyAnnouncementsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.ANNOUNCEMENT_TABLE);
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView announcements;
    private User user;
    private List<Announcement> data;

    public MyAnnouncementsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_my_announcements, container, false);

        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout);
        announcements = root.findViewById(R.id.announcements);
        Session session = new Session(getActivity());
        user = session.getUser();
        data = new ArrayList<>();
        announcements.setLayoutManager(new LinearLayoutManager(getActivity()));
        loadData();

        return root;
    }

    private void loadData() {
        swipeRefreshLayout.setRefreshing(true);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onRefresh() {
        loadData();
    }
}