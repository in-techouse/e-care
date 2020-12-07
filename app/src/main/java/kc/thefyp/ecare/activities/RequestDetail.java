package kc.thefyp.ecare.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

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

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import kc.thefyp.ecare.R;
import kc.thefyp.ecare.activities.ui.main.SectionsPagerAdapter;
import kc.thefyp.ecare.director.Constants;
import kc.thefyp.ecare.director.Helpers;
import kc.thefyp.ecare.models.Notification;
import kc.thefyp.ecare.models.Request;

public class RequestDetail extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "RequestDetail";
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    private ValueEventListener listener;
    private Notification notification;
    private Request request;
    private ProgressDialog dialog;
    private SectionsPagerAdapter sectionsPagerAdapter;

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

        CircularProgressButton acceptRequest = findViewById(R.id.acceptRequest);
        acceptRequest.setOnClickListener(this);

        dialog = new ProgressDialog(RequestDetail.this);
        dialog.setTitle("LOADING!");
        dialog.setMessage("Please wait...!");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

        if (request == null) {
            // Load request from notification
            Log.e(TAG, "Notification is not NULL, Request is NULL");
            Log.e(TAG, "Going to load request from notification");
            loadRequestFromNotification();
        } else {
            Log.e(TAG, "Request is not NULL, Notification is NULL");
            new CountDownTimer(2000, 2000) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    sectionsPagerAdapter.setRequest(request);
                }
            }.start();
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
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (snapshot.exists()) {
                    request = snapshot.getValue(Request.class);
                    if (request != null) {
                        Log.e(TAG, "Request is loaded: " + request.getId());
                        sectionsPagerAdapter.setRequest(request);
                    }
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.acceptRequest: {
                break;
            }
        }
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