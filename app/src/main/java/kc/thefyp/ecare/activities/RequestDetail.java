package kc.thefyp.ecare.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import java.util.Date;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import kc.thefyp.ecare.R;
import kc.thefyp.ecare.activities.ui.main.SectionsPagerAdapter;
import kc.thefyp.ecare.director.Constants;
import kc.thefyp.ecare.director.Helpers;
import kc.thefyp.ecare.director.Session;
import kc.thefyp.ecare.models.Donation;
import kc.thefyp.ecare.models.Notification;
import kc.thefyp.ecare.models.Request;
import kc.thefyp.ecare.models.User;
import kc.thefyp.ecare.services.NotificationService;

public class RequestDetail extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "RequestDetail";
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    private ValueEventListener listener, donationValueListener;
    private Notification notification;
    private Request request;
    private ProgressDialog dialog;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private LinearLayout acceptRequestUpper;
    private User currentUser, donationUser;
    private CircularProgressButton acceptRequest, rejectRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_detail);

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

        request = (Request) bundle.getSerializable("request");
        notification = (Notification) bundle.getSerializable("notification");
        if (request == null && notification == null) {
            Log.e(TAG, "Request & Notification both are null");
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        acceptRequestUpper = findViewById(R.id.acceptRequestUpper);
        acceptRequest = findViewById(R.id.acceptRequest);
        acceptRequest.setOnClickListener(this);
        rejectRequest = findViewById(R.id.rejectRequest);
        rejectRequest.setOnClickListener(this);

        dialog = new ProgressDialog(RequestDetail.this);
        dialog.setTitle("LOADING!");
        dialog.setMessage("Please wait...!");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        Session session = new Session(getApplicationContext());
        currentUser = session.getUser();

        if (request == null) {
            // Load request from notification
            Log.e(TAG, "Notification is not NULL, Request is NULL");
            Log.e(TAG, "Going to load request from notification");
            loadRequestFromNotification();
        } else {
            Log.e(TAG, "Request is not NULL, Notification is NULL");
            if (!currentUser.getId().equals(request.getToUser())) {
                acceptRequestUpper.setVisibility(View.GONE);
            } else if (!request.getStatus().equals("Requested")) {
                acceptRequestUpper.setVisibility(View.GONE);
            }
            loadDonationUser();
        }
    }

    private void loadRequestFromNotification() {
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e(TAG, "Request loaded Success");
                if (listener != null) {
                    reference.child(Constants.REQUEST_TABLE).child(notification.getId()).removeEventListener(listener);
                }
                if (listener != null) {
                    reference.removeEventListener(listener);
                }
                if (snapshot.exists()) {
                    request = snapshot.getValue(Request.class);
                    if (request != null) {
                        Log.e(TAG, "Request is loaded: " + request.getId());
                        if (!currentUser.getId().equals(request.getToUser())) {
                            acceptRequestUpper.setVisibility(View.GONE);
                        } else if (!request.getStatus().equals("Requested")) {
                            acceptRequestUpper.setVisibility(View.GONE);
                        }
                        loadDonationUser();
                    } else {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Helpers.showErrorWithActivityClose(RequestDetail.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
                    }
                } else {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Helpers.showErrorWithActivityClose(RequestDetail.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Request loaded Failed");
                if (listener != null) {
                    reference.child(Constants.REQUEST_TABLE).child(notification.getId()).removeEventListener(listener);
                }
                if (listener != null) {
                    reference.removeEventListener(listener);
                }
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                Helpers.showErrorWithActivityClose(RequestDetail.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
            }
        };

        reference.child(Constants.REQUEST_TABLE).child(notification.getId()).addValueEventListener(listener);
    }

    private void loadDonationUser() {
        reference.child(Constants.USER_TABLE).child(request.getFromUser())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        if (snapshot.exists()) {
                            donationUser = snapshot.getValue(User.class);
                            if (donationUser != null) {
                                sectionsPagerAdapter.setRequest(request);
                            } else {
                                Helpers.showErrorWithActivityClose(RequestDetail.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
                            }
                        } else {
                            Helpers.showErrorWithActivityClose(RequestDetail.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Helpers.showErrorWithActivityClose(RequestDetail.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
                    }
                });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.acceptRequest: {
                acceptRequest();
                break;
            }
            case R.id.rejectRequest: {
                rejectRequest();
                break;
            }
        }
    }

    private void acceptRequest() {
        acceptRequest.startAnimation();
        request.setStatus("Accepted");
        reference.child(Constants.REQUEST_TABLE).child(request.getId()).setValue(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        String notificationMessage = "Hey " + donationUser.getName() + "! " + currentUser.getName() + " accepted your request for the donation.";
                        NotificationService.sendNotificationToUser(getApplicationContext(), notificationMessage, request.getId(), "RequestAccepted", donationUser.getId());
                        Notification notification = new Notification();
                        final String nId = reference.child(Constants.NOTIFICATIONS_TABLE).push().getKey();
                        notification.setId(nId);
                        notification.setMessage(notificationMessage);
                        notification.setType("RequestAccepted");
                        notification.setUserId(donationUser.getId());
                        reference.child(Constants.NOTIFICATIONS_TABLE).child(notification.getId()).setValue(notification);
                        updateDonation();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        revertAcceptAnimation();
                        Helpers.showError(RequestDetail.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
                    }
                });
    }

    private void updateDonation() {
        donationValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (donationValueListener != null) {
                    reference.child(Constants.DONATION_TABLE).child(request.getDonationId()).removeEventListener(donationValueListener);
                }
                if (donationValueListener != null) {
                    reference.removeEventListener(donationValueListener);
                }
                if (snapshot.exists()) {
                    Donation donation = snapshot.getValue(Donation.class);
                    if (donation != null) {
                        donation.setDonated(true);
                        donation.setDonatedTo(donationUser.getId());
                        donation.setTimestamps(new Date().getTime());
                        reference.child(Constants.DONATION_TABLE).child(request.getDonationId()).setValue(donation);
                        revertAcceptAnimation();
                        Helpers.showSuccessWithActivityClose(RequestDetail.this, "", Constants.REQUEST_ACCEPTED);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (donationValueListener != null) {
                    reference.child(Constants.DONATION_TABLE).child(request.getDonationId()).removeEventListener(donationValueListener);
                }
                if (donationValueListener != null) {
                    reference.removeEventListener(donationValueListener);
                }
                revertAcceptAnimation();
                Helpers.showSuccessWithActivityClose(RequestDetail.this, "", Constants.REQUEST_ACCEPTED);
            }
        };

        reference.child(Constants.DONATION_TABLE).child(request.getDonationId()).addValueEventListener(donationValueListener);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void revertAcceptAnimation() {
        acceptRequest.revertAnimation();
        acceptRequest.setBackground(getResources().getDrawable(R.drawable.rounded_button));
    }

    private void rejectRequest() {
        rejectRequest.startAnimation();
        request.setStatus("Rejected");
        reference.child(Constants.REQUEST_TABLE).child(request.getId()).setValue(request)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        String notificationMessage = "Hey " + donationUser.getName() + "! " + currentUser.getName() + " rejected your request for the donation.";
                        NotificationService.sendNotificationToUser(getApplicationContext(), notificationMessage, request.getId(), "RequestRejected", donationUser.getId());
                        Notification notification = new Notification();
                        final String nId = reference.child(Constants.NOTIFICATIONS_TABLE).push().getKey();
                        notification.setId(nId);
                        notification.setMessage(notificationMessage);
                        notification.setType("RequestRejected");
                        notification.setUserId(donationUser.getId());
                        reference.child(Constants.NOTIFICATIONS_TABLE).child(notification.getId()).setValue(notification);
                        revertRejectAnimation();
                        Helpers.showSuccessWithActivityClose(RequestDetail.this, "", Constants.REQUEST_REJECTED);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        revertRejectAnimation();
                        Helpers.showError(RequestDetail.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
                    }
                });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void revertRejectAnimation() {
        rejectRequest.revertAnimation();
        rejectRequest.setBackground(getResources().getDrawable(R.drawable.danger_rounded_button));
    }

    @Override
    public void onBackPressed() {
        finishActivity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finishActivity();
                break;
            }
        }
        return true;
    }

    private void finishActivity() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        finish();
    }
}