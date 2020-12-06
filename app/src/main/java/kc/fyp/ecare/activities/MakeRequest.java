package kc.fyp.ecare.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import de.hdodenhof.circleimageview.CircleImageView;
import kc.fyp.ecare.R;
import kc.fyp.ecare.director.Constants;
import kc.fyp.ecare.director.Helpers;
import kc.fyp.ecare.director.Session;
import kc.fyp.ecare.models.Donation;
import kc.fyp.ecare.models.Request;
import kc.fyp.ecare.models.User;
import kc.fyp.ecare.services.NotificationService;

public class MakeRequest extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MakeRequest";
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    private ValueEventListener listener;
    private CircularProgressButton action_send_request;
    private User donationUser, currentUser;
    private Donation donation;
    private EditText edtName, edtDescription;
    private Request request;
    private int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_request);

        Intent it = getIntent();
        if (it == null) {
            Log.e(TAG, "Intent is null");
            finish();
            return;
        }
        Bundle bundle = it.getExtras();
        if (bundle == null) {
            Log.e(TAG, "Bundle is null");
            finish();
            return;
        }
        donation = (Donation) bundle.getSerializable("donation");
        if (donation == null) {
            Log.e(TAG, "Donation is null");
            finish();
            return;
        }
        donationUser = (User) bundle.getSerializable("donationUser");
        if (donationUser == null) {
            Log.e(TAG, "Donation User is null");
            finish();
            return;
        }

        Session session = new Session(getApplicationContext());
        currentUser = session.getUser();

        // Set User Detail
        CircleImageView userImage = findViewById(R.id.userImage);
        if (donationUser.getImage() != null && donationUser.getImage().length() > 0) {
            Glide.with(getApplicationContext()).load(donationUser.getImage()).into(userImage);
        }
        TextView userName = findViewById(R.id.userName);
        userName.setText(donationUser.getName());

        // Set Donation detail
        TextView name = findViewById(R.id.name);
        TextView category = findViewById(R.id.category);
        TextView description = findViewById(R.id.description);
        TextView address = findViewById(R.id.address);
        RelativeLayout directions = findViewById(R.id.directions);
        name.setText(donation.getName());
        category.setText(donation.getCategory());
        description.setText(donation.getDescription());
        address.setText(donation.getAddress());

        // Set Current User detail
        edtName = findViewById(R.id.edtName);
        edtDescription = findViewById(R.id.edtDescription);
        edtName.setText(currentUser.getName());

        // Send Request detail
        request = new Request();
        String rId = reference.child(Constants.REQUEST_TABLE).push().getKey();
        request.setId(rId);
        request.setDonationId(donation.getId());
        request.setToUser(donationUser.getId());
        request.setFromUser(currentUser.getId());

        action_send_request = findViewById(R.id.action_send_request);
        action_send_request.setOnClickListener(this);
        directions.setOnClickListener(this);
        loadUserDonationsCount();
    }

    private void loadUserDonationsCount() {
        count = 0;
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        if (data.getValue() != null) {
                            Log.e(TAG, "Value found");
                            count++;
                        }
                    }
                    if (count > 4) {
                        Helpers.showErrorWithActivityClose(MakeRequest.this, Constants.ERROR, Constants.CANNOT_REQUEST_MORE_DONATIONS);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        reference.child(Constants.DONATION_TABLE).orderByChild("donatedTo").equalTo(currentUser.getId()).addValueEventListener(listener);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.directions: {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + donation.getLatitude() + "," + donation.getLongitude() + "");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
                break;
            }
            case R.id.action_send_request: {
                // Check Internet Connection
                if (!Helpers.isConnected(getApplicationContext())) {
                    Helpers.showError(MakeRequest.this, Constants.LOGIN_FAILED, Constants.NO_INTERNET);
                    return;
                }
                if (!isValid()) {
                    startSaving();
                }
                break;
            }
        }
    }

    private void startSaving() {
        action_send_request.startAnimation();
        edtName.setEnabled(false);
        edtDescription.setEnabled(false);
        request.setTimestamps(new Date().getTime());
        reference.child(Constants.REQUEST_TABLE).child(request.getId()).setValue(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Send a notification to donation poster
                        String notificationMessage = "Hey " + donationUser.getName() + "!, " + currentUser.getName() + " submitted a request for your donation you posted.";
                        NotificationService.sendNotificationToUser(getApplicationContext(), notificationMessage, request.getId(), "ViewRequest", donationUser.getId());
                        // Stop progress, and enable all inputs.
                        stopSaving();
                        // Show user a success message that request is submitted/
                        Helpers.showSuccessWithActivityClose(MakeRequest.this, Constants.POSTED, Constants.REQUEST_POSTED + donationUser.getName());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Stop progress, and enable all inputs.
                        stopSaving();
                        // Show user an error that donation request is not posted.
                        Helpers.showError(MakeRequest.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
                    }
                });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void stopSaving() {
        action_send_request.revertAnimation();
        action_send_request.setBackground(getResources().getDrawable(R.drawable.rounded_button));
        edtName.setEnabled(true);
        edtDescription.setEnabled(true);
    }

    private boolean isValid() {
        boolean flag = false;
        String strName = edtName.getText().toString();
        String strDescription = edtDescription.getText().toString();

        if (strDescription.length() < 10) {
            edtDescription.setError(Constants.DESCRIPTION_ERROR);
            edtDescription.requestFocus();
            flag = true;
        } else {
            edtDescription.setError(null);
        }

        if (strName.length() < 3) {
            edtName.setError(Constants.NAME_ERROR);
            edtName.requestFocus();
            flag = true;
        } else {
            edtName.setError(null);
        }

        request.setUserName(strName);
        request.setDescription(strDescription);

        return flag;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            reference.child(Constants.DONATION_TABLE).orderByChild("donatedTo").equalTo(currentUser.getId()).removeEventListener(listener);
        }
        if (listener != null) {
            reference.removeEventListener(listener);
        }
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