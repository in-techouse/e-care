package kc.thefyp.ecare.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import kc.thefyp.ecare.R;
import kc.thefyp.ecare.director.Constants;
import kc.thefyp.ecare.models.Request;
import kc.thefyp.ecare.models.User;

public class RequestDetailFragment extends Fragment {
    private static final String TAG = "RequestDetailFragment";
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.USER_TABLE);
    private ValueEventListener listener;
    private Request request;
    private LinearLayout mainRoot;
    private TextView description, userName;
    private CardView userCard;

    public RequestDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_request_detail, container, false);
        mainRoot = root.findViewById(R.id.mainRoot);
        mainRoot.setVisibility(View.GONE);
        description = root.findViewById(R.id.description);
        userCard = root.findViewById(R.id.userCard);
        userCard.setVisibility(View.GONE);
        userName = root.findViewById(R.id.userName);
        return root;
    }

    public void setRequest(Request request) {
        this.request = request;
        Log.e(TAG, "Request received");
        Log.e(TAG, "Request id is: " + this.request.getId());
//        description.setText(request.getDescription());
//        mainRoot.setVisibility(View.VISIBLE);
//        loadUserDetail();
    }

    private void loadUserDetail() {
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (listener != null) {
                    reference.child(request.getFromUser()).removeEventListener(listener);
                }
                if (listener != null) {
                    reference.removeEventListener(listener);
                }
                if (snapshot.exists()) {
                    User fromUser = snapshot.getValue(User.class);
                    if (fromUser != null) {
                        userName.setText(fromUser.getName());
                        userCard.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (listener != null) {
                    reference.child(request.getFromUser()).removeEventListener(listener);
                }
                if (listener != null) {
                    reference.removeEventListener(listener);
                }
            }
        };
        reference.child(request.getFromUser()).addValueEventListener(listener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listener != null) {
            reference.child(request.getFromUser()).removeEventListener(listener);
        }
        if (listener != null) {
            reference.removeEventListener(listener);
        }
    }
}