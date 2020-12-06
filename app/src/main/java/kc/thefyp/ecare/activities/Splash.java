package kc.thefyp.ecare.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.rbddevs.splashy.Splashy;

import kc.thefyp.ecare.R;
import kc.thefyp.ecare.director.Session;
import kc.thefyp.ecare.models.User;

public class Splash extends AppCompatActivity {
    private static final String TAG = "Splash";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Splashy.OnComplete() {
            @Override
            public void onComplete() {
                Log.e(TAG, "OnComplete Call in onCreate");
                // Check User Session.
                // If user is logged in, take the user to dashboard, otherwise take the user on LoginScreen.
                Session session = new Session(Splash.this);
                User user = session.getUser();
                Intent it;
                if (user == null) {
                    it = new Intent(Splash.this, Login.class);
                } else {
                    it = new Intent(Splash.this, Dashboard.class);
                }
                startActivity(it);
                finish();
            }
        }.onComplete();
        setSplashy();
    }

    private void setSplashy() {
        new Splashy(this)
                .setLogo(R.drawable.logo)
                .setTitle(R.string.app_name)
                .setTitleColor(R.color.colorPrimaryDark)
                .setSubTitle(R.string.app_intro)
                .setSubTitleColor(R.color.colorWhite)
                .setProgressColor(R.color.colorPrimaryDark)
                .setBackgroundResource(R.color.colorPrimary)
                .setFullScreen(true)
                .setTime(2500)
                .showProgress(true)
                .setAnimation(Splashy.Animation.SLIDE_IN_LEFT_BOTTOM, 1500)
                .show();
    }
}