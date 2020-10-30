package kc.fyp.ecare.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;
import com.smarteist.autoimageslider.SliderViewAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import kc.fyp.ecare.R;
import kc.fyp.ecare.director.Constants;
import kc.fyp.ecare.director.Helpers;
import kc.fyp.ecare.director.Session;
import kc.fyp.ecare.models.Donation;
import kc.fyp.ecare.models.User;

public class NewDonation extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "NewDonation";
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.DONATION_TABLE);
    private User user;
    private Session session;
    private Donation donation;
    private SliderAdapter adapter;
    private SliderView imageSlider;
    private List<Uri> donationImages;
    private CircularProgressButton action_save;
    private RelativeLayout selectAddress;
    private Spinner category;
    private TextView address;
    private EditText edtName, edtQuantity, edtDescription, edtContact;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_donation);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout toolBarLayout = findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize all variables
        session = new Session(NewDonation.this);
        user = session.getUser();
        donation = new Donation();
        String id = reference.push().getKey();
        donation.setId(id);
        donation.setUserId(user.getId());
        donationImages = new ArrayList<>();

        fab = findViewById(R.id.fab);
        action_save = findViewById(R.id.action_save);
        imageSlider = findViewById(R.id.imageSlider);
        category = findViewById(R.id.category);
        selectAddress = findViewById(R.id.selectAddress);
        address = findViewById(R.id.address);
        edtName = findViewById(R.id.edtName);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtDescription = findViewById(R.id.edtDescription);
        edtContact = findViewById(R.id.edtContact);
        fab.setOnClickListener(this);
        action_save.setOnClickListener(this);
        selectAddress.setOnClickListener(this);

        adapter = new SliderAdapter();
        imageSlider.setSliderAdapter(adapter);
        imageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        imageSlider.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
        imageSlider.setIndicatorSelectedColor(Color.WHITE);
        imageSlider.setIndicatorUnselectedColor(Color.GRAY);
        imageSlider.setScrollTimeInSec(4);
        imageSlider.startAutoCycle();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.fab: {
                if (askForPermission()) {
                    openGallery();
                }
                break;
            }
            case R.id.selectAddress: {
                Intent it = new Intent(NewDonation.this, SelectAddress.class);
                startActivityForResult(it, 10);
                break;
            }
            case R.id.action_save: {
                if (!Helpers.isConnected(NewDonation.this)) {
                    Helpers.showError(NewDonation.this, Constants.ERROR, Constants.NO_INTERNET);
                    return;
                }
                // Check Donation Validation
                if (isValidDonation()) {
                    Log.e(TAG, "Images List is: " + donationImages.size());
                    Log.e(TAG, "Address is: " + donation.getAddress());
                    Log.e(TAG, "Latitude is: " + donation.getLatitude());
                    Log.e(TAG, "Longitude is: " + donation.getLongitude());
                    Log.e(TAG, "Name is: " + donation.getName());
                    Log.e(TAG, "Quantity is: " + donation.getQuantity());
                    Log.e(TAG, "Description is: " + donation.getDescription());
                    Log.e(TAG, "Contact is: " + donation.getContact());
                    Log.e(TAG, "Id is: " + donation.getId());
                    Log.e(TAG, "User Id is: " + donation.getUserId());
                    startSaving();
                }
                break;
            }
        }
    }

    private void startSaving() {
        action_save.startAnimation();
        edtName.setEnabled(false);
        edtQuantity.setEnabled(false);
        edtDescription.setEnabled(false);
        edtContact.setEnabled(false);
        category.setEnabled(false);
        category.setClickable(false);
        fab.setEnabled(false);
        fab.setClickable(false);
        selectAddress.setEnabled(false);
        selectAddress.setClickable(false);
        if (donationImages.size() > 0) {
            uploadImage(0);
        } else {
            saveToDatabase();
        }
    }

    private void uploadImage(final int count) {
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Constants.DONATION_TABLE).child(donation.getId());
        Calendar calendar = Calendar.getInstance();
        storageReference.child(calendar.getTimeInMillis() + "").putFile(donationImages.get(count))
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.e(TAG, "in OnSuccess: " + uri.toString());
                                        donation.getImages().add(uri.toString());
                                        if (donation.getImages().size() == donationImages.size()) {
                                            saveToDatabase();
                                        } else {
                                            uploadImage(count + 1);
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "DownloadUrl:" + e.getMessage());
                                        stopSaving();
                                        Helpers.showError(NewDonation.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "UploadImageUrl:" + e.getMessage());
                        stopSaving();
                        Helpers.showError(NewDonation.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
                    }
                });
    }

    private void saveToDatabase() {
        reference.child(donation.getId()).setValue(donation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        stopSaving();
                        Helpers.showSuccessWithActivityClose(NewDonation.this, Constants.POSTED, Constants.DONATION_POSTED);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Save to database failed:" + e.getMessage());
                        stopSaving();
                        Helpers.showError(NewDonation.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
                    }
                });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void stopSaving() {
        action_save.revertAnimation();
        action_save.setBackground(getResources().getDrawable(R.drawable.rounded_button));
        edtName.setEnabled(true);
        edtQuantity.setEnabled(true);
        edtDescription.setEnabled(true);
        edtContact.setEnabled(true);
        category.setEnabled(true);
        category.setClickable(true);
        fab.setEnabled(true);
        fab.setClickable(true);
        selectAddress.setEnabled(true);
        selectAddress.setClickable(true);
    }

    private boolean isValidDonation() {
        boolean flag = true;
        String error = "";
        String strName = edtName.getText().toString();
        String strQuantity = edtQuantity.getText().toString();
        String strDescription = edtDescription.getText().toString();
        String strContact = edtContact.getText().toString();
        String strAddress = address.getText().toString();
        String strCategory = category.getSelectedItem().toString();

        if (category.getSelectedItemPosition() == 0) {
            error = error + "*Choose the donation category\n";
            flag = false;
        } else {
            donation.setCategory(strCategory);
        }

        if (strAddress.equals(getResources().getString(R.string.select_address))) {
            error = error + "*Choose the donation pickup address\n";
            flag = false;
        }

        if (strName.length() < 3) {
            edtName.setError(Constants.NAME_ERROR);
            flag = false;
        } else {
            edtName.setError(null);
            donation.setName(strName);
        }

        if (strQuantity.length() < 1) {
            edtQuantity.setError(Constants.QUANTITY_ERROR);
            flag = false;
        } else {
            edtQuantity.setError(null);
            donation.setQuantity(Integer.parseInt(strQuantity));
        }

        if (strDescription.length() < 3) {
            edtDescription.setError(Constants.DESCRIPTION_ERROR);
            flag = false;
        } else {
            edtDescription.setError(null);
            donation.setDescription(strDescription);
        }

        if (strContact.length() != 11) {
            edtContact.setError(Constants.PHONE_NUMBER_ERROR);
            flag = false;
        } else {
            edtContact.setError(null);
            donation.setContact(strContact);
        }

        if (error.length() > 0) {
            Helpers.showError(NewDonation.this, Constants.ERROR, error);
        }
        return flag;
    }

    private boolean askForPermission() {
        if (ActivityCompat.checkSelfPermission(NewDonation.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NewDonation.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(NewDonation.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
            return false;
        }
        return true;
    }

    private void openGallery() {
        ImagePicker.create(NewDonation.this)
                .toolbarImageTitle("Tap to select")
                .multi()
                .limit(3)
                .showCamera(true)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            donationImages.clear();
            adapter.notifyDataSetChanged();
            List<Image> images = ImagePicker.getImages(data);
            List<Uri> uriList = new ArrayList<>();
            for (Image img : images) {
                Uri uri = Uri.fromFile(new File(img.getPath()));
                uriList.add(uri);
            }
            donationImages = uriList;
            imageSlider.setSliderAdapter(adapter);
            imageSlider.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
            imageSlider.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH);
            imageSlider.setIndicatorSelectedColor(Color.WHITE);
            imageSlider.setIndicatorUnselectedColor(Color.GRAY);
            imageSlider.setScrollTimeInSec(4);
            imageSlider.startAutoCycle();
            adapter.notifyDataSetChanged();
        } else if (requestCode == 10 && resultCode == RESULT_OK) {
            if (data != null) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    Donation d = (Donation) bundle.getSerializable("result");
                    if (d != null) {
                        Log.e(TAG, "Location Received: " + d.getAddress());
                        donation.setLatitude(d.getLatitude());
                        donation.setLongitude(d.getLongitude());
                        donation.setAddress(d.getAddress());
                        address.setText(donation.getAddress());
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
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

    private class SliderAdapter extends SliderViewAdapter<SliderAdapter.SliderAdapterVH> {
        public SliderAdapter() {
        }

        @Override
        public SliderAdapterVH onCreateViewHolder(ViewGroup parent) {
            View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_layout_item, null);
            return new SliderAdapterVH(inflate);
        }

        @Override
        public void onBindViewHolder(SliderAdapterVH viewHolder, int position) {
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