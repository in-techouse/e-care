package kc.fyp.ecare.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import kc.fyp.ecare.R;
import kc.fyp.ecare.director.Session;
import kc.fyp.ecare.models.User;

public class EditProfile extends AppCompatActivity implements View.OnClickListener {
    private CircularProgressButton action_update;
    private User user;
    private Session session;
    private ImageView image;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout toolBarLayout = findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        session = new Session(EditProfile.this);
        user = session.getUser();

        FloatingActionButton fab = findViewById(R.id.fab);
        action_update = findViewById(R.id.action_update);
        image = findViewById(R.id.image);
        fab.setOnClickListener(this);
        action_update.setOnClickListener(this);

        if (user.getImage() != null && user.getImage().length() > 0) {
            Glide.with(getApplicationContext()).load(user.getImage()).into(image);
        } else {
            image.setImageDrawable(getResources().getDrawable(R.drawable.user));
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.fab: {
                break;
            }
            case R.id.action_update: {
                break;
            }
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