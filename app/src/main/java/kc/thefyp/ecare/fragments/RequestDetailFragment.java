package kc.thefyp.ecare.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import kc.thefyp.ecare.R;
import kc.thefyp.ecare.director.Constants;
import kc.thefyp.ecare.models.Request;
import kc.thefyp.ecare.models.User;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class RequestDetailFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "RequestDetailFragment";
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.USER_TABLE);
    private ValueEventListener listener;
    private Request request;
    private LinearLayout mainRoot;
    private TextView description, requestTime, userName, userEmail, userContact;
    private CardView userCard;
    private CircleImageView userImage;
    private User fromUser;
    private MaterialRatingBar ratingBar;

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
        requestTime = root.findViewById(R.id.requestTime);
        userCard = root.findViewById(R.id.userCard);
        userCard.setVisibility(View.GONE);
        userName = root.findViewById(R.id.userName);
        userEmail = root.findViewById(R.id.userEmail);
        userContact = root.findViewById(R.id.userContact);
        userImage = root.findViewById(R.id.userImage);
        ratingBar = root.findViewById(R.id.ratingBar);
        RelativeLayout callUser = root.findViewById(R.id.callUser);
        callUser.setOnClickListener(this);
        return root;
    }

    public void setRequest(Request request) {
        this.request = request;
        Log.e(TAG, "Request received");
        Log.e(TAG, "Request id is: " + this.request.getId());
        description.setText(request.getDescription());
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(request.getTimestamps());
        String date = DateFormat.format("EEEE, dd, MMM yyyy hh:mm:ss", cal).toString();
        requestTime.setText(date);
        description.setText(request.getDescription());
        mainRoot.setVisibility(View.VISIBLE);
        loadUserDetail();
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
                    fromUser = snapshot.getValue(User.class);
                    if (fromUser != null) {
                        if (fromUser.getImage() != null && fromUser.getImage().length() > 0) {
                            Glide.with(getActivity()).load(fromUser.getImage()).into(userImage);
                        }
                        userName.setText(fromUser.getName());
                        userEmail.setText(fromUser.getEmail());
                        userContact.setText(fromUser.getPhoneNumber());
                        ratingBar.setRating(Float.parseFloat(fromUser.getRating() + ""));
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.callUser: {
                if (fromUser != null) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + fromUser.getPhoneNumber()));
                    startActivity(intent);
                }
                break;
            }
        }
    }
}