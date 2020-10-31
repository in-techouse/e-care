package kc.fyp.ecare.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;
import com.shashank.sony.fancydialoglib.Icon;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import kc.fyp.ecare.R;
import kc.fyp.ecare.director.Constants;
import kc.fyp.ecare.director.Helpers;
import kc.fyp.ecare.director.Session;
import kc.fyp.ecare.models.User;

public class EditProfile extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "EditProfile";
    private final DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.USER_TABLE);
    private CircularProgressButton action_update;
    private User user;
    private Session session;
    private ImageView image;
    private EditText edtName;
    private FloatingActionButton fab;
    private Uri imageUri = null;

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

        fab = findViewById(R.id.fab);
        action_update = findViewById(R.id.action_update);
        image = findViewById(R.id.image);
        fab.setOnClickListener(this);
        action_update.setOnClickListener(this);
        edtName = findViewById(R.id.edtName);
        EditText edtEmail = findViewById(R.id.edtEmail);
        EditText edtPhone = findViewById(R.id.edtPhone);

        if (user.getImage() != null && user.getImage().length() > 0) {
            Glide.with(getApplicationContext()).load(user.getImage()).into(image);
        } else {
            image.setImageDrawable(getResources().getDrawable(R.drawable.user));
        }

        edtName.setText(user.getName());
        edtEmail.setText(user.getEmail());
        edtPhone.setText(user.getPhoneNumber());
        edtEmail.setEnabled(false);
        edtPhone.setEnabled(false);
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
            case R.id.action_update: {
                if (!Helpers.isConnected(EditProfile.this)) {
                    Helpers.showError(EditProfile.this, Constants.ERROR, Constants.NO_INTERNET);
                    return;
                }
                // Check Profile Validation
                if (isValid()) {
                    startTask();
                }
                break;
            }
        }
    }

    private void startTask() {
        action_update.startAnimation();
        edtName.setEnabled(false);
        fab.setEnabled(false);
        fab.setClickable(false);
        if (imageUri != null) {
            uploadImage();
        } else {
            saveToDatabase();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void stopTask() {
        action_update.revertAnimation();
        action_update.setBackground(getResources().getDrawable(R.drawable.rounded_button));
        edtName.setEnabled(true);
        fab.setEnabled(true);
        fab.setClickable(true);
    }

    private void uploadImage() {
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(Constants.USER_TABLE).child(user.getId());
        Calendar calendar = Calendar.getInstance();
        storageReference.child(calendar.getTimeInMillis() + "").putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.e(TAG, "in OnSuccess: " + uri.toString());
                                        user.setImage(uri.toString());
                                        saveToDatabase();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "DownloadUrl:" + e.getMessage());
                                        stopTask();
                                        Helpers.showError(EditProfile.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "UploadImageUrl:" + e.getMessage());
                        stopTask();
                        Helpers.showError(EditProfile.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
                    }
                });
    }

    private void saveToDatabase() {
        reference.child(user.getId()).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        session.setSession(user);
                        stopTask();
                        showUpdateProfileSuccess();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Save to database failed:" + e.getMessage());
                        stopTask();
                        Helpers.showError(EditProfile.this, Constants.ERROR, Constants.SOMETHING_WENT_WRONG);
                    }
                });
    }

    private void showUpdateProfileSuccess() {
        new FancyAlertDialog.Builder(EditProfile.this)
                .setTitle("Your Profile has been updated successfully.")
                .setMessage("")
                .setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark))
                .setNegativeBtnText("CLOSE")
                .setNegativeBtnBackground(getResources().getColor(R.color.colorDanger))
                .setPositiveBtnText("OKAY")
                .setPositiveBtnBackground(getResources().getColor(R.color.colorPrimary))
                .setAnimation(Animation.POP)
                .isCancellable(false)
                .setIcon(R.drawable.ic_action_okay, Icon.Visible)
                .OnPositiveClicked(new FancyAlertDialogListener() {
                    @Override
                    public void OnClick() {
                        startAgain();
                    }
                })
                .OnNegativeClicked(new FancyAlertDialogListener() {
                    @Override
                    public void OnClick() {
                        startAgain();
                    }
                })
                .build();
    }

    private void startAgain() {
        Intent it = new Intent(EditProfile.this, Dashboard.class);
        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(it);
        finish();
    }

    private boolean isValid() {
        boolean flag = true;
        String strName = edtName.getText().toString();
        if (strName.length() < 3) {
            edtName.setError(Constants.NAME_ERROR);
            flag = false;
        } else {
            edtName.setError(null);
            user.setName(strName);
        }
        return flag;
    }

    private boolean askForPermission() {
        if (ActivityCompat.checkSelfPermission(EditProfile.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(EditProfile.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditProfile.this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 10);
            return false;
        }
        return true;
    }

    private void openGallery() {
        ImagePicker.create(EditProfile.this)
                .toolbarImageTitle("Tap to select")
                .single()
                .showCamera(true)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (ImagePicker.shouldHandle(requestCode, resultCode, data)) {
            Log.e(TAG, "ImagePicker Call back received, inside if");
            List<Image> images = ImagePicker.getImages(data);
            Log.e(TAG, "ImagePicker Call back received, inside if, Images Length is: " + images.size());
            if (images != null && images.size() > 0) {
                imageUri = Uri.fromFile(new File(images.get(0).getPath()));
                Glide.with(getApplicationContext()).load(imageUri).into(image);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
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