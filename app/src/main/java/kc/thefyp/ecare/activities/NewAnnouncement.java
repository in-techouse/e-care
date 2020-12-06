package kc.thefyp.ecare.activities;

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
import kc.thefyp.ecare.R;
import kc.thefyp.ecare.director.Constants;
import kc.thefyp.ecare.director.Helpers;
import kc.thefyp.ecare.director.Session;
import kc.thefyp.ecare.models.Announcement;
import kc.thefyp.ecare.models.Donation;
import kc.thefyp.ecare.models.User;
import kc.thefyp.ecare.services.NotificationService;

public class NewAnnouncement extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "NewAnnouncement";
    // Firebase database reference, to save the new announcement data to database.
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.ANNOUNCEMENT_TABLE);
    // Model class, for announcement
    private Announcement announcement;
    // To show selected images in adapter.
    private SliderAdapter adapter;
    // From XML file
    private SliderView imageSlider;
    // To store the user selected images, temporarily.
    private List<Uri> announcementImages;
    // Button
    private CircularProgressButton action_save;
    private RelativeLayout selectAddress;
    private Spinner category;
    private TextView address;
    private EditText edtName, edtDescription, edtContact;
    // To open gallery
    private FloatingActionButton fab;
    // To save the value of current user
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_announcement);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        CollapsingToolbarLayout toolBarLayout = findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize all variables
        Session session = new Session(NewAnnouncement.this);
        user = session.getUser(); // Get value of current logged in user.
        announcement = new Announcement();
        // Get id of announcement from firebase database.
        String id = reference.push().getKey();
        announcement.setId(id);
        announcement.setUserId(user.getId());
        announcementImages = new ArrayList<>();

        // Find view by id all widgets
        fab = findViewById(R.id.fab);
        action_save = findViewById(R.id.action_save);
        imageSlider = findViewById(R.id.imageSlider);
        category = findViewById(R.id.category);
        selectAddress = findViewById(R.id.selectAddress);
        address = findViewById(R.id.address);
        edtName = findViewById(R.id.edtName);
        edtDescription = findViewById(R.id.edtDescription);
        edtContact = findViewById(R.id.edtContact);

        // Set click listener on buttons
        fab.setOnClickListener(this);
        action_save.setOnClickListener(this);
        selectAddress.setOnClickListener(this);

        // Slider adapter, to show all the selected images in the slider with animation
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
            // Use clicks the fab button to select images
            case R.id.fab: {
                // Check for user permissions
                if (askForPermission()) { // True => Permission granted, False => Permission Reject
                    // Open the gallery for user, if permission is granted.
                    openGallery();
                }
                break;
            }
            // User clicks the location icon to select address
            case R.id.selectAddress: {
                // Start a new activity so the user can select the address
                Intent it = new Intent(NewAnnouncement.this, SelectAddress.class);
                startActivityForResult(it, 10);
                break;
            }
            // User clicks the save button to post the announcement
            case R.id.action_save: {
                // Check internet connection
                if (!Helpers.isConnected(NewAnnouncement.this)) {
                    Helpers.showError(NewAnnouncement.this, Constants.ERROR, Constants.NO_INTERNET);
                    return;
                }
                // Check Announcement Validation
                if (isValidAnnouncement()) { // True => Every input is good, False => Inputs have errors
                    Log.e(TAG, "Images List is: " + announcementImages.size());
                    Log.e(TAG, "Address is: " + announcement.getAddress());
                    Log.e(TAG, "Latitude is: " + announcement.getLatitude());
                    Log.e(TAG, "Longitude is: " + announcement.getLongitude());
                    Log.e(TAG, "Name is: " + announcement.getName());
                    Log.e(TAG, "Description is: " + announcement.getDescription());
                    Log.e(TAG, "Contact is: " + announcement.getContact());
                    Log.e(TAG, "Id is: " + announcement.getId());
                    Log.e(TAG, "User Id is: " + announcement.getUserId());
                    // Start saving the announcement
                    startSaving();
                }
                break;
            }
        }
    }

    // Start saving the announcement
    private void startSaving() {
        // Convert button into loading bar
        action_save.startAnimation();
        // Disable all input fields
        edtName.setEnabled(false);
        edtDescription.setEnabled(false);
        edtContact.setEnabled(false);
        category.setEnabled(false);
        category.setClickable(false);
        fab.setEnabled(false);
        fab.setClickable(false);
        selectAddress.setEnabled(false);
        selectAddress.setClickable(false);
        // Announcement have images, first we have to upload the images, then we will save the announcement
        if (announcementImages.size() > 0) {
            // Upload announcement image to firebase
            uploadImage(0);
        } else { // Announcement don't have images, just save the announcement
            // Save the announcement to database.
            saveToDatabase();
        }
    }

    // Check Announcement Validation
    private boolean isValidAnnouncement() {
        boolean flag = true;
        String error = "";
        String strName = edtName.getText().toString();
        String strDescription = edtDescription.getText().toString();
        String strContact = edtContact.getText().toString();
        String strAddress = address.getText().toString();
        String strCategory = category.getSelectedItem().toString();

        if (category.getSelectedItemPosition() == 0) {
            error = error + "*Choose the announcement category\n";
            flag = false;
        } else {
            announcement.setCategory(strCategory);
        }

        if (strAddress.equals(getResources().getString(R.string.required_at_address))) {
            error = error + "*Choose the announcement address\n";
            flag = false;
        }

        if (strName.length() < 3) {
            edtName.setError(Constants.NAME_ERROR);
            flag = false;
        } else {
            edtName.setError(null);
            announcement.setName(strName);
        }

        if (strDescription.length() < 3) {
            edtDescription.setError(Constants.DESCRIPTION_ERROR);
            flag = false;
        } else {
            edtDescription.setError(null);
            announcement.setDescription(strDescription);
        }

        if (strContact.length() != 11) {
            edtContact.setError(Constants.PHONE_NUMBER_ERROR);
            flag = false;
        } else {
            edtContact.setError(null);
            announcement.setContact(strContact);
        }

        if (error.length() > 0) {
            Helpers.showError(NewAnnouncement.this, Constants.ERROR, error);
        }
        return flag;
    }

    // Upload announcement image to firebase
    private void uploadImage(final int count) {
        // Firebase Storage Reference
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Constants.ANNOUNCEMENT_TABLE).child(announcement.getId());
        // Using calender to get a unique name of file every time.
        Calendar calendar = Calendar.getInstance();
        storageReference.child(calendar.getTimeInMillis() + "").putFile(announcementImages.get(count))
                // File upload success listener
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get the download URL of uploaded file.
                        taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.e(TAG, "in OnSuccess: " + uri.toString());
                                        announcement.getImages().add(uri.toString()); // Save the download URL
                                        // If user have selected more then one image, then upload the other image first.
                                        // If selected images count is equal to uploaded images count, the save the announcement, otherwise upload the next image.
                                        if (announcement.getImages().size() == announcementImages.size()) {
                                            // Save the announcement to database.
                                            saveToDatabase();
                                        } else {
                                            // Upload next image
                                            uploadImage(count + 1);
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "DownloadUrl:" + e.getMessage());
                                        // Stop the progress bar, and enable all inputs
                                        stopSaving();
                                        // Show user an error that announcement is not saved.
                                        Helpers.showError(NewAnnouncement.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
                                    }
                                });
                    }
                })
                // File upload failure listener
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "UploadImageUrl:" + e.getMessage());
                        // Stop the progress bar, and enable all inputs
                        stopSaving();
                        // Show user an error that announcement is not saved.
                        Helpers.showError(NewAnnouncement.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
                    }
                });
    }

    // Save the announcement to database.
    private void saveToDatabase() {
        reference.child(announcement.getId()).setValue(announcement)
                // Value save success function
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Stop the progress bar, and enable all inputs
                        stopSaving();
                        // Send to notification to all users
                        String notificationMessage = user.getName() + " posted an announcement. Check it out, if you can help.";
                        NotificationService.sendNotificationToAllUsers(getApplicationContext(), notificationMessage, announcement.getId(), "ViewAnnouncement", user.getId());
                        // Show success message to user, that the announcement have been posted successfully.
                        Helpers.showSuccessWithActivityClose(NewAnnouncement.this, Constants.POSTED, Constants.ANNOUNCEMENT_POSTED);
                    }
                })
                // Value save failure function
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Save to database failed:" + e.getMessage());
                        // Stop the progress bar, and enable all inputs
                        stopSaving();
                        // Show user an error that announcement is not saved.
                        Helpers.showError(NewAnnouncement.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
                    }
                });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void stopSaving() {
        action_save.revertAnimation();
        action_save.setBackground(getResources().getDrawable(R.drawable.rounded_button));
        edtName.setEnabled(true);
        edtDescription.setEnabled(true);
        edtContact.setEnabled(true);
        category.setEnabled(true);
        category.setClickable(true);
        fab.setEnabled(true);
        fab.setClickable(true);
        selectAddress.setEnabled(true);
        selectAddress.setClickable(true);
    }

    // Check for user permissions
    private boolean askForPermission() {
        if (ActivityCompat.checkSelfPermission(NewAnnouncement.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NewAnnouncement.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(NewAnnouncement.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
            return false;
        }
        return true;
    }

    // Open the gallery for user, if permission is granted.
    private void openGallery() {
        ImagePicker.create(NewAnnouncement.this)
                .toolbarImageTitle("Tap to select")
                .multi()
                .limit(3)
                .showCamera(true)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            announcementImages.clear();
            adapter.notifyDataSetChanged();
            List<Image> images = ImagePicker.getImages(data);
            List<Uri> uriList = new ArrayList<>();
            for (Image img : images) {
                Uri uri = Uri.fromFile(new File(img.getPath()));
                uriList.add(uri);
            }
            announcementImages = uriList;
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
                        announcement.setLatitude(d.getLatitude());
                        announcement.setLongitude(d.getLongitude());
                        announcement.setAddress(d.getAddress());
                        address.setText(announcement.getAddress());
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