package kc.fyp.ecare.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.rbddevs.splashy.Splashy;

import kc.fyp.ecare.R;

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
                Intent it = new Intent(Splash.this, Login.class);
                startActivity(it);
                finish();
//                Session session = new Session(SplashActivity.this);
//                User user = session.getUser();
//                if (user == null) {
//                    Intent It = new Intent(SplashActivity.this, LoginActivity.class);
//                    startActivity(It);
//                    finish();
//                } else {
//                    if (user.getType() == 0) {
//                        Intent It = new Intent(SplashActivity.this, DashBoard.class);
//                        startActivity(It);
//                    } else {
//                        Intent It = new Intent(SplashActivity.this, DriverDashboard.class);
//                        startActivity(It);
//                    }
//                    finish();
//                }
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