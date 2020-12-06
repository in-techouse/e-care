package kc.fyp.ecare.activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MenuItem;

import kc.fyp.ecare.R;
import kc.fyp.ecare.activities.ui.main.SectionsPagerAdapter;
import kc.fyp.ecare.models.Notification;
import kc.fyp.ecare.models.Request;

public class RequestDetail extends AppCompatActivity {
    private static final String TAG = "RequestDetail";
    private Notification notification;
    private Request request;

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

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
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