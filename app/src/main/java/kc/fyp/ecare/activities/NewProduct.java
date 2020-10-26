package kc.fyp.ecare.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.smarteist.autoimageslider.SliderView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import kc.fyp.ecare.R;
import kc.fyp.ecare.director.Session;
import kc.fyp.ecare.models.User;

public class NewProduct extends AppCompatActivity implements View.OnClickListener {
    private CircularProgressButton action_save;
    private User user;
    private Session session;
    private SliderView imageSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_product);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        session = new Session(NewProduct.this);
        user = session.getUser();

        FloatingActionButton fab = findViewById(R.id.fab);
        action_save = findViewById(R.id.action_save);
        imageSlider = findViewById(R.id.imageSlider);
        fab.setOnClickListener(this);
        action_save.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.fab: {
                break;
            }
            case R.id.action_save: {
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