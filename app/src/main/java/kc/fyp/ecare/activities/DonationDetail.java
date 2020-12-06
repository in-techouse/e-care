package kc.fyp.ecare.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
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
import androidx.appcompat.widget.AppCompatButton;
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
import kc.fyp.ecare.R;
import kc.fyp.ecare.director.Constants;
import kc.fyp.ecare.director.Helpers;
import kc.fyp.ecare.director.Session;
import kc.fyp.ecare.models.Donation;
import kc.fyp.ecare.models.User;

public class DonationDetail extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "DonationDetail";
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    private ValueEventListener userValueListener;
    private Donation donation;
    private List<String> donationImages;
    private TextView userName, userEmail, userContact;
    private ProgressBar userProgress;
    private LinearLayout userLayout;
    private User donationUser;
    private CircleImageView userImage;

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

        // Initialize all variables
        Session session = new Session(DonationDetail.this);
        User user = session.getUser();

        // Donation Variables
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
        RelativeLayout callUser = findViewById(R.id.callUser);
        RelativeLayout directions = findViewById(R.id.directions);
        RelativeLayout contactDetail = findViewById(R.id.contactDetail);
        View contactDetailDivider = findViewById(R.id.contactDetailDivider);

        LinearLayout makeRequestUpper = findViewById(R.id.makeRequestUpper);
        AppCompatButton makeRequest = findViewById(R.id.makeRequest);

        donationImages = donation.getImages();
        SliderAdapter adapter = new SliderAdapter();
        imageSlider.setSliderAdapter(adapter);
        imageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        imageSlider.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
        imageSlider.setIndicatorSelectedColor(Color.WHITE);
        imageSlider.setIndicatorUnselectedColor(Color.GRAY);
        imageSlider.setScrollTimeInSec(4);
        imageSlider.startAutoCycle();

        // Show Donation Detail
        name.setText(donation.getName());
        category.setText(donation.getCategory());
        description.setText(donation.getDescription());
        address.setText(donation.getAddress());
        contact.setText(donation.getContact());

        callUser.setOnClickListener(this);
        directions.setOnClickListener(this);
        makeRequest.setOnClickListener(this);

        contactDetail.setVisibility(View.GONE);
        callUser.setVisibility(View.GONE);
        contactDetailDivider.setVisibility(View.GONE);

        if (user.getId().equals(donation.getUserId())) {
            makeRequestUpper.setVisibility(View.GONE);
            contactDetail.setVisibility(View.VISIBLE);
            contactDetailDivider.setVisibility(View.VISIBLE);
            callUser.setVisibility(View.VISIBLE);
        } else if (user.getId().equals(donation.getDonatedTo())) {
            contactDetail.setVisibility(View.VISIBLE);
            contactDetailDivider.setVisibility(View.VISIBLE);
            callUser.setVisibility(View.VISIBLE);
        }

        // Load Donation Owner Detail
        loadOwnerDetail();
    }

    private void loadOwnerDetail() {
        userValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue() != null) {
                    donationUser = snapshot.getValue(User.class);
                    if (donationUser != null) {
                        userLayout.setVisibility(View.VISIBLE);
                        userName.setText(donationUser.getName());
                        userEmail.setText(donationUser.getEmail());
                        userContact.setText(donationUser.getPhoneNumber());
                        if (donationUser.getImage() != null && donationUser.getImage().length() > 0) {
                            Glide.with(getApplicationContext()).load(donationUser.getImage()).into(userImage);
                        }
                    }
                }
                userProgress.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Helpers.showError(DonationDetail.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
                userProgress.setVisibility(View.GONE);
            }
        };
        reference.child(Constants.USER_TABLE).child(donation.getUserId()).addValueEventListener(userValueListener);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.callUser: {
                if (donationUser != null) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + donationUser.getPhoneNumber()));
                    startActivity(intent);
                }
                break;
            }
            case R.id.directions: {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + donation.getLatitude() + "," + donation.getLongitude() + "");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
                break;
            }
            case R.id.makeRequest: {
                Intent it = new Intent(DonationDetail.this, MakeRequest.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("donation", donation);
                bundle.putSerializable("donationUser", donationUser);
                it.putExtras(bundle);
                startActivity(it);
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
            reference.child(Constants.USER_TABLE).child(donation.getUserId()).removeEventListener(userValueListener);
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