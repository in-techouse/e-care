package kc.fyp.ecare.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.smarteist.autoimageslider.SliderViewAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import kc.fyp.ecare.R;
import kc.fyp.ecare.director.Session;
import kc.fyp.ecare.models.Donation;
import kc.fyp.ecare.models.User;

public class DonationDetail extends AppCompatActivity {
    private static final String TAG = "DonationDetail";
    private Donation donation;
    private List<String> donationImages;
    private SliderAdapter adapter;
    private SliderView imageSlider;
    private TextView name, category, description, address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout toolBarLayout = findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent it = getIntent();
        if (it == null) {
            Log.e(TAG, "Intent is null");
            finish();
        }
        Bundle bundle = it.getExtras();
        if (bundle == null) {
            Log.e(TAG, "Bundle is null");
            finish();
        }
        donation = (Donation) bundle.getSerializable("donation");
        if (donation == null) {
            Log.e(TAG, "Donation is null");
            finish();
        }

        // Initialize all variables
        Session session = new Session(DonationDetail.this);
        User user = session.getUser();
        imageSlider = findViewById(R.id.imageSlider);
        name = findViewById(R.id.name);
        category = findViewById(R.id.category);
        description = findViewById(R.id.description);
        address = findViewById(R.id.address);
        donationImages = donation.getImages();
        adapter = new SliderAdapter();
        imageSlider.setSliderAdapter(adapter);
        imageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        imageSlider.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
        imageSlider.setIndicatorSelectedColor(Color.WHITE);
        imageSlider.setIndicatorUnselectedColor(Color.GRAY);
        imageSlider.setScrollTimeInSec(4);
        imageSlider.startAutoCycle();

        name.setText(donation.getName());
        category.setText(donation.getCategory());
        description.setText(donation.getDescription());
        address.setText(donation.getAddress());
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

    private class SliderAdapter extends SliderViewAdapter<SliderAdapter.SliderAdapterVH> {
        public SliderAdapter() {
        }

        @Override
        public SliderAdapter.SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, null);
            return new SliderAdapter.SliderAdapterVH(inflate);
        }

        @Override
        public void onBindViewHolder(SliderAdapter.SliderAdapterVH viewHolder, int position) {
            Glide.with(viewHolder.itemView)
                    .load(donationImages.get(position))
                    .into(viewHolder.imageViewBackground);
        }

        @Override
        public int getCount() {
            return donationImages.size();
        }

        class SliderAdapterVH extends SliderViewAdapter.ViewHolder {
            View itemView;
            ImageView imageViewBackground;

            public SliderAdapterVH(View itemView) {
                super(itemView);
                imageViewBackground = itemView.findViewById(R.id.iv_auto_image_slider);
                this.itemView = itemView;
            }
        }
    }
}