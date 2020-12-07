package kc.thefyp.ecare.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.smarteist.autoimageslider.SliderViewAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kc.thefyp.ecare.R;
import kc.thefyp.ecare.director.Constants;
import kc.thefyp.ecare.director.Helpers;
import kc.thefyp.ecare.director.Session;
import kc.thefyp.ecare.models.Announcement;
import kc.thefyp.ecare.models.User;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class AnnouncementDetail extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AnnouncementDetail";
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    private ValueEventListener userValueListener;
    private Announcement announcement;
    private List<String> announcementImages;
    private TextView userName, userEmail, userContact;
    private ProgressBar userProgress;
    private LinearLayout userLayout;
    private User announcementUser;
    private CircleImageView userImage;
    private MaterialRatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_detail);

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
            return;
        }
        Bundle bundle = it.getExtras();
        if (bundle == null) {
            Log.e(TAG, "Bundle is null");
            finish();
            return;
        }
        announcement = (Announcement) bundle.getSerializable("announcement");
        if (announcement == null) {
            Log.e(TAG, "Announcement is null");
            finish();
            return;
        }

        // Initialize all variables
        Session session = new Session(AnnouncementDetail.this);
        User user = session.getUser();

        // Announcement Variables
        SliderView imageSlider = findViewById(R.id.imageSlider);
        TextView name = findViewById(R.id.name);
        TextView category = findViewById(R.id.category);
        TextView description = findViewById(R.id.description);
        TextView address = findViewById(R.id.address);
        TextView contact = findViewById(R.id.contact);

        // User Variables
        userProgress = findViewById(R.id.userProgress);
        userLayout = findViewById(R.id.userLayout);
        userName = findViewById(R.id.userName);
        userImage = findViewById(R.id.userImage);
        userEmail = findViewById(R.id.userEmail);
        userContact = findViewById(R.id.userContact);
        ratingBar = findViewById(R.id.ratingBar);
        RelativeLayout callUser = findViewById(R.id.callUser);
        RelativeLayout directions = findViewById(R.id.directions);

        announcementImages = announcement.getImages();
        SliderAdapter adapter = new SliderAdapter();
        imageSlider.setSliderAdapter(adapter);
        imageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        imageSlider.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
        imageSlider.setIndicatorSelectedColor(Color.WHITE);
        imageSlider.setIndicatorUnselectedColor(Color.GRAY);
        imageSlider.setScrollTimeInSec(4);
        imageSlider.startAutoCycle();

        // Show Announcement Detail
        name.setText(announcement.getName());
        category.setText(announcement.getCategory());
        description.setText(announcement.getDescription());
        address.setText(announcement.getAddress());
        contact.setText(announcement.getContact());

        callUser.setOnClickListener(this);
        directions.setOnClickListener(this);
        // Load Announcement Owner Detail
        loadOwnerDetail();
    }

    private void loadOwnerDetail() {
        userValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    announcementUser = snapshot.getValue(User.class);
                    if (announcementUser != null) {
                        userLayout.setVisibility(View.VISIBLE);
                        userName.setText(announcementUser.getName());
                        userEmail.setText(announcementUser.getEmail());
                        userContact.setText(announcementUser.getPhoneNumber());
                        if (announcementUser.getImage() != null && announcementUser.getImage().length() > 0) {
                            Glide.with(getApplicationContext()).load(announcementUser.getImage()).into(userImage);
                        }
                        ratingBar.setRating(Float.parseFloat(announcementUser.getRating() + ""));
                    }
                }
                userProgress.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Helpers.showError(AnnouncementDetail.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
                userProgress.setVisibility(View.GONE);
            }
        };
        reference.child(Constants.USER_TABLE).child(announcement.getUserId()).addValueEventListener(userValueListener);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.callUser: {
                if (announcementUser != null) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + announcementUser.getPhoneNumber()));
                    startActivity(intent);
                }
                break;
            }
            case R.id.directions: {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + announcement.getLatitude() + "," + announcement.getLongitude() + "");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userValueListener != null) {
            reference.child(Constants.USER_TABLE).child(announcement.getUserId()).removeEventListener(userValueListener);
        }
        if (userValueListener != null) {
            reference.removeEventListener(userValueListener);
        }
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
                    .load(announcementImages.get(position))
                    .into(viewHolder.imageViewBackground);
        }

        @Override
        public int getCount() {
            return announcementImages.size();
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