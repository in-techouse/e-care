package kc.fyp.ecare.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import kc.fyp.ecare.R;
import kc.fyp.ecare.models.Donation;
import kc.fyp.ecare.models.User;

public class MakeRequest extends AppCompatActivity {
    private static final String TAG = "MakeRequest";
    private Donation donation;
    private User donationUser;

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