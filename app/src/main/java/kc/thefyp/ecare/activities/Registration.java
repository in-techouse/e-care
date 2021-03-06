package kc.thefyp.ecare.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;
import com.shashank.sony.fancydialoglib.Icon;

import java.util.concurrent.TimeUnit;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import kc.thefyp.ecare.R;
import kc.thefyp.ecare.director.Constants;
import kc.thefyp.ecare.director.Helpers;
import kc.thefyp.ecare.models.User;

public class Registration extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Registration";
    private EditText edtName, edtEmail, edtPhone, edtPassword, edtPasswordConfirmation;
    private String strName, strEmail, strPhone, strPassword;
    private CountryCodePicker ccp;
    private CircularProgressButton action_register;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtPasswordConfirmation = findViewById(R.id.edtPasswordConfirmation);
        ccp = findViewById(R.id.ccp);
        action_register = findViewById(R.id.action_register);

        ccp.registerCarrierNumberEditText(edtPhone);

        // Set Click Listener on Register Button
        action_register.setOnClickListener(this);
    }

    // Function to disable all inputs, and start progress
    private void disableInputs() {
        // Disable all inputs
        edtName.setEnabled(false);
        edtEmail.setEnabled(false);
        ccp.setEnabled(false);
        edtPhone.setEnabled(false);
        edtPassword.setEnabled(false);
        edtPasswordConfirmation.setEnabled(false);
        // Start button progress
        action_register.startAnimation();
    }

    // Enable all inputs, and stop the progress
    @SuppressLint("UseCompatLoadingForDrawables")
    private void enableInputs() {
        // Enable all inputs
        edtName.setEnabled(true);
        edtEmail.setEnabled(true);
        ccp.setEnabled(true);
        edtPhone.setEnabled(true);
        edtPassword.setEnabled(true);
        edtPasswordConfirmation.setEnabled(true);
        // Stop progress of button
        action_register.revertAnimation();
        action_register.setBackground(getResources().getDrawable(R.drawable.rounded_button));
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.action_register: {
                // Check Internet Connection
                if (!Helpers.isConnected(getApplicationContext())) {
                    Helpers.showError(Registration.this, Constants.REGISTRATION_FAILED, Constants.NO_INTERNET);
                    return;
                }
                // Check if data is valid or not
                boolean flag = isValid(); // Function to check, all values are valid or not
                if (flag) {
                    disableInputs(); // Function to disable all inputs, and start progress
                    // Set User Detail
                    user = new User();
                    user.setId("");
                    user.setImage("");
                    user.setName(strName);
                    user.setEmail(strEmail);
                    user.setPhoneNumber(strPhone);
                    user.setVerified(false);
                    // First do the email registration
                    emailRegistration();
                }
                break;
            }
        }
    }

    // Function to check, all values are valid or not
    private boolean isValid() {
        boolean flag = true;
        strName = edtName.getText().toString();
        strEmail = edtEmail.getText().toString();
        strPhone = ccp.getFullNumberWithPlus();
        strPassword = edtPassword.getText().toString();
        String strPasswordConfirmation = edtPasswordConfirmation.getText().toString();

        if (strName.length() < 3) {
            edtName.setError(Constants.NAME_ERROR);
            flag = false;
        } else {
            edtName.setError(null);
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()) {
            edtEmail.setError(Constants.EMAIL_ERROR);
            flag = false;
        } else {
            edtEmail.setError(null);
        }

        if (!ccp.isValidFullNumber()) {
            edtPhone.setError(Constants.PHONE_NUMBER_ERROR);
            flag = false;
        } else {
            edtPhone.setError(null);
        }

        if (strPassword.length() < 6) {
            edtPassword.setError(Constants.PASSWORD_ERROR);
            flag = false;
        } else {
            edtPassword.setError(null);
        }

        if (strPassword.length() > 5 && !strPassword.equals(strPasswordConfirmation)) {
            edtPasswordConfirmation.setError(Constants.PASSWORD_CONFIRMATION_ERROR);
            flag = false;
        } else {
            edtPasswordConfirmation.setError(null);
        }

        return flag;
    }

    // Email Registration of User
    private void emailRegistration() {
        auth.createUserWithEmailAndPassword(strEmail, strPassword)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.e(TAG, "Email Registration Success");
                        if (auth.getCurrentUser() != null) { // Registration is successful
                            user.setId(auth.getCurrentUser().getUid()); // Set user id
                            // Send Verification OTP
                            sendOtp();
                        } else { // Registration failed
                            enableInputs(); // Enable all inputs, and stop the progress
                            Helpers.showError(Registration.this, Constants.REGISTRATION_FAILED, Constants.SOMETHING_WENT_WRONG);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Email Registration Failed: " + e.getMessage());
                        enableInputs(); // Enable all inputs, and stop the progress
                        Helpers.showError(Registration.this, Constants.REGISTRATION_FAILED, e.getMessage());
                    }
                });
    }

    // Send Verification OTP
    private void sendOtp() {
        PhoneAuthProvider.OnVerificationStateChangedCallbacks callBack;
        callBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);
                Log.e(TAG, "OTP is sent successfully");
                enableInputs(); // Enable all inputs, and stop the progress
                // Show Sent OTP Success Message
                showOTPSuccess(Constants.PHONE_NUMBER_VERIFICATION, Constants.OTP_SENT + user.getPhoneNumber(), verificationId, forceResendingToken);
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                Log.e(TAG, "User verified automatically,");
                // Link User Account with Phone Number
                linkWithPhoneNumber(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Log.e(TAG, "OTP Send failed, with error: " + e.getMessage());
                enableInputs(); // Enable all inputs, and stop the progress
                Helpers.showError(Registration.this, Constants.REGISTRATION_FAILED, e.getMessage());
            }
        };
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(user.getPhoneNumber())
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(callBack)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // Show Sent OTP Success Message
    public void showOTPSuccess(String title, String message, final String verificationId, final PhoneAuthProvider.ForceResendingToken forceResendingToken) {
        new FancyAlertDialog.Builder(Registration.this)
                .setTitle(title)
                .setMessage(message)
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
                        Intent it = new Intent(Registration.this, OTPVerification.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("user", user);
                        bundle.putBoolean("isRegistration", true);
                        bundle.putString("verificationId", verificationId);
                        bundle.putParcelable("resendToken", forceResendingToken);
                        it.putExtras(bundle);
                        startActivity(it);
                        finish();
                    }
                })
                .OnNegativeClicked(new FancyAlertDialogListener() {
                    @Override
                    public void OnClick() {
                        Intent it = new Intent(Registration.this, OTPVerification.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("user", user);
                        bundle.putBoolean("isRegistration", true);
                        bundle.putString("verificationId", verificationId);
                        bundle.putParcelable("resendToken", forceResendingToken);
                        it.putExtras(bundle);
                        startActivity(it);
                        finish();
                    }
                })
                .build();
    }

    // Link User Account with Phone Number
    private void linkWithPhoneNumber(PhoneAuthCredential credential) {
        if (auth.getCurrentUser() != null) {
            auth.getCurrentUser().linkWithCredential(credential)
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            // Send Verification Email
                            sendVerificationEmail();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            enableInputs();
                            Helpers.showError(Registration.this, Constants.REGISTRATION_FAILED, e.getMessage());
                        }
                    });
        } else {
            enableInputs();
            Helpers.showError(Registration.this, Constants.REGISTRATION_FAILED, Constants.SOMETHING_WENT_WRONG);
        }
    }

    // Send Verification Email
    private void sendVerificationEmail() {
        if (auth.getCurrentUser() != null) {
            auth.getCurrentUser().sendEmailVerification()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // Save user detail to database
                            saveUserToDatabase();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            enableInputs();
                            Helpers.showError(Registration.this, Constants.REGISTRATION_FAILED, e.getMessage());
                        }
                    });
        } else {
            enableInputs();
            Helpers.showError(Registration.this, Constants.REGISTRATION_FAILED, Constants.SOMETHING_WENT_WRONG);
        }
    }

    // Save user detail to database
    private void saveUserToDatabase() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.USER_TABLE);
        reference.child(user.getId()).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        enableInputs();
                        Helpers.showSuccess(Registration.this, Constants.REGISTRATION_COMPLETED, Constants.REGISTRATION_SUCCESS);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        enableInputs();
                        Helpers.showError(Registration.this, Constants.REGISTRATION_FAILED, Constants.SOMETHING_WENT_WRONG);
                    }
                });
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