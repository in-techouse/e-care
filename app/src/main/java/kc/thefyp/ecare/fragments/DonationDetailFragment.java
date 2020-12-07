package kc.thefyp.ecare.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.List;

import kc.thefyp.ecare.R;
import kc.thefyp.ecare.activities.DonationDetail;
import kc.thefyp.ecare.director.Constants;
import kc.thefyp.ecare.models.Donation;
import kc.thefyp.ecare.models.Request;

public class DonationDetailFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "DonationDetailFragment";
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.DONATION_TABLE);
    private ValueEventListener valueEventListener;
    private Request request;
    private LinearLayout mainRoot;
    private Donation donation;
    private SliderView imageSlider;
    private List<String> donationImages;
    private TextView name, category, description, address, contact;

    public DonationDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_donation_detail, container, false);
        mainRoot = root.findViewById(R.id.mainRoot);
        mainRoot.setVisibility(View.GONE);

        // Donation Variables
        imageSlider = root.findViewById(R.id.imageSlider);
        name = root.findViewById(R.id.name);
        category = root.findViewById(R.id.category);
        description = root.findViewById(R.id.description);
        address = root.findViewById(R.id.address);
        contact = root.findViewById(R.id.contact);
        RelativeLayout directions = root.findViewById(R.id.directions);

        directions.setOnClickListener(this);
        return root;
    }

    public void setRequest(Request request) {
        this.request = request;
        Log.e(TAG, "Request received");
        Log.e(TAG, "Request id is: " + this.request.getId());
        loadDonation();
    }

    private void loadDonation() {
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (valueEventListener != null) {
                    reference.child(request.getDonationId()).removeEventListener(valueEventListener);
                }
                if (valueEventListener != null) {
                    reference.removeEventListener(valueEventListener);
                }
                if (snapshot.exists()) {
                    donation = snapshot.getValue(Donation.class);
                    if (donation != null) {
                        // Show Donation Detail
                        name.setText(donation.getName());
                        category.setText(donation.getCategory());
                        description.setText(donation.getDescription());
                        address.setText(donation.getAddress());
                        contact.setText(donation.getContact());
                        donationImages = donation.getImages();
                        SliderAdapter adapter = new SliderAdapter();
                        imageSlider.setSliderAdapter(adapter);
                        imageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
                        imageSlider.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
                        imageSlider.setIndicatorSelectedColor(Color.WHITE);
                        imageSlider.setIndicatorUnselectedColor(Color.GRAY);
                        imageSlider.setScrollTimeInSec(4);
                        imageSlider.startAutoCycle();
                        mainRoot.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (valueEventListener != null) {
                    reference.child(request.getDonationId()).removeEventListener(valueEventListener);
                }
                if (valueEventListener != null) {
                    reference.removeEventListener(valueEventListener);
                }
            }
        };
        reference.child(request.getDonationId()).addValueEventListener(valueEventListener);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.directions: {
                if (donation != null) {
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + donation.getLatitude() + "," + donation.getLongitude() + "");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
                break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (valueEventListener != null) {
            reference.child(request.getDonationId()).removeEventListener(valueEventListener);
        }
        if (valueEventListener != null) {
            reference.removeEventListener(valueEventListener);
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