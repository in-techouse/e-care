package kc.fyp.ecare.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.chaos.view.PinView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;
import com.shashank.sony.fancydialoglib.Icon;

import java.util.concurrent.TimeUnit;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import kc.fyp.ecare.R;
import kc.fyp.ecare.director.Constants;
import kc.fyp.ecare.director.Helpers;
import kc.fyp.ecare.director.Session;
import kc.fyp.ecare.models.User;

public class OTPVerification extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "OTPVerification";
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private User user;
    private PinView firstPinView;
    private TextView timer, action_resend;
    private CircularProgressButton action_verify_otp;
    private boolean isRegistration;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private CountDownTimer countDownTimer;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_o_t_p_verification);

        // Check Intent
        Intent it = getIntent();
        if (it == null) {
            Log.e(TAG, "Intent is Null");
            finish();
            return;
        }
        // Check Bundle
        Bundle bundle = it.getExtras();
        if (bundle == null) {
            Log.e(TAG, "Bundle is Null");
            finish();
            return;
        }
        // Check Verification Id
        verificationId = bundle.getString("verificationId");
        if (verificationId == null) {
            Log.e(TAG, "Verification is Null");
            finish();
            return;
        }
        // Check Resend Token
        resendToken = bundle.getParcelable("resendToken");
        if (resendToken == null) {
            Log.e(TAG, "Refresh Token is Null");
            finish();
            return;
        }
        // Check User
        user = (User) bundle.getSerializable("user");
        if (user == null) {
            Log.e(TAG, "User is Null");
            finish();
            return;
        }
        isRegistration = bundle.getBoolean("isRegistration");

        TextView enterOtpText = findViewById(R.id.enterOtpText);
        enterOtpText.setText("Enter the OTP code received at, " + user.getPhoneNumber());

        firstPinView = findViewById(R.id.firstPinView);
        timer = findViewById(R.id.timer);
        action_resend = findViewById(R.id.action_resend);
        action_verify_otp = findViewById(R.id.action_verify_otp);
        action_resend.setOnClickListener(this);
        action_verify_otp.setOnClickListener(this);
        startTimer();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void revertButton() {
        action_verify_otp.revertAnimation();
        action_verify_otp.setBackground(getResources().getDrawable(R.drawable.rounded_button));
    }

    // Start Resend Timer
    private void startTimer() {
        action_resend.setEnabled(false);
        countDownTimer = new CountDownTimer(120000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                millisUntilFinished = millisUntilFinished / 1000;
                long seconds = millisUntilFinished % 60;
                long minutes = (millisUntilFinished / 60) % 60;
                String time = "";
                if (seconds > 9) {
                    time = "0" + minutes + ":" + seconds;
                } else {
                    time = "0" + minutes + ":" + "0" + seconds;
                }
                timer.setText(time);
            }

            @Override
            public void onFinish() {
                timer.setText("--:--");
                action_resend.setEnabled(true);
            }
        };
        countDownTimer.start();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.action_resend: {
                // Check Internet Connection
                if (!Helpers.isConnected(getApplicationContext())) {
                    Helpers.showError(OTPVerification.this, Constants.REGISTRATION_FAILED, Constants.NO_INTERNET);
                    return;
                }
                action_verify_otp.startAnimation(); // This line will convert the button to circular progress bar
                // Send OTP Again
                sendOtpAgain();
                break;
            }
            case R.id.action_verify_otp: {
                // Check Internet Connection
                if (!Helpers.isConnected(getApplicationContext())) {
                    Helpers.showError(OTPVerification.this, Constants.REGISTRATION_FAILED, Constants.NO_INTERNET);
                    return;
                }
                // Check the user have entered the OTP code or not.
                if (firstPinView == null || firstPinView.getText() == null) {
                    firstPinView.setError(Constants.ERROR_INVALID_OTP);
                    return;
                }
                String otp = firstPinView.getText().toString();
                if (otp.length() != 6) {
                    firstPinView.setError(Constants.ERROR_INVALID_OTP);
                    return;
                }
                // If the user entered the OTP, verify it from Firebase.
                action_verify_otp.startAnimation(); // This line will convert the button to circular progress bar
                firstPinView.setError(null);
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
                if (isRegistration) { // User is in the Registration Process, Register the user
                    // Link User Account with Phone Number
                    linkWithPhoneNumber(credential);
                } else { // The user is already registered, the app should have to logged in the user
                    signInUser(credential);
                }
                break;
            }
        }
    }

    // Send OTP Again
    private void sendOtpAgain() {
        PhoneAuthProvider provider = PhoneAuthProvider.getInstance();
        PhoneAuthProvider.OnVerificationStateChangedCallbacks callBack;
        callBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onCodeSent(@NonNull String vId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);
                // Show Sent OTP Success Message
                revertButton(); // Convert the circular progress bar to button again.
                // Restart the Timer.
                verificationId = vId;
                resendToken = forceResendingToken;
                startTimer();
                Helpers.showSuccess(OTPVerification.this, Constants.PHONE_NUMBER_VERIFICATION, Constants.OTP_SENT + user.getPhoneNumber());
            }

            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                if (isRegistration) { // User is in the Registration Process, Register the user
                    // Link User Account with Phone Number
                    linkWithPhoneNumber(phoneAuthCredential);
                } else { // The user is already registered, the app should have to logged in the user
                    signInUser(phoneAuthCredential);
                }
            }

            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                revertButton(); // Convert the circular progress bar to button again.
                countDownTimer.cancel();
                timer.setText("--:--");
                action_resend.setEnabled(true);
                Helpers.showError(OTPVerification.this, Constants.PHONE_NUMBER_VERIFICATION, e.getMessage());
            }
        };
        provider.verifyPhoneNumber(user.getPhoneNumber(), 120, TimeUnit.SECONDS, this, callBack);
    }

    // SignIn User Automatically
    private void signInUser(PhoneAuthCredential phoneAuthCredential) {
        auth.signInWithCredential(phoneAuthCredential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        if (auth.getCurrentUser() != null) {
                            if (auth.getCurrentUser().isEmailVerified()) {
                                // Set Session and move to Dashboard
                                Session session = new Session(getApplicationContext());
                                user.setVerified(true);
                                session.setSession(user);
                                Log.e(TAG, "User Login is Successful");
                                Intent it = new Intent(OTPVerification.this, Dashboard.class);
                                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(it);
                            } else {
                                revertButton();
                                auth.getCurrentUser().sendEmailVerification();
                                Helpers.showError(OTPVerification.this, Constants.LOGIN_FAILED, Constants.EMAIL_NOT_VERIFIED_ERROR);
                                auth.signOut();
                            }

                        } else {
                            revertButton();
                            Helpers.showError(OTPVerification.this, Constants.LOGIN_FAILED, Constants.SOMETHING_WENT_WRONG);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        revertButton();
                        Helpers.showError(OTPVerification.this, Constants.LOGIN_FAILED, e.getMessage());
                    }
                });
    }

    // Link User Account with Phone Number
    @SuppressLint("UseCompatLoadingForDrawables")
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
                        @SuppressLint("UseCompatLoadingForDrawables")
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            revertButton();
                            Helpers.showError(OTPVerification.this, Constants.REGISTRATION_FAILED, e.getMessage());
                        }
                    });
        } else {
            revertButton();
            Helpers.showError(OTPVerification.this, Constants.REGISTRATION_FAILED, Constants.SOMETHING_WENT_WRONG);
        }
    }

    // Send Verification Email
    @SuppressLint("UseCompatLoadingForDrawables")
    private void sendVerificationEmail() {
        if (auth.getCurrentUser() != null) {
            auth.getCurrentUser().sendEmailVerification()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.e(TAG, "Verification Email is Sent");
                            // Save user detail to database
                            saveUserToDatabase();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @SuppressLint("UseCompatLoadingForDrawables")
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            revertButton();
                            Helpers.showError(OTPVerification.this, Constants.REGISTRATION_FAILED, e.getMessage());
                        }
                    });
        } else {
            revertButton();
            Helpers.showError(OTPVerification.this, Constants.REGISTRATION_FAILED, Constants.SOMETHING_WENT_WRONG);
        }
    }

    // Save user detail to database
    private void saveUserToDatabase() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.USER_TABLE);
        reference.child(user.getId()).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @SuppressLint("UseCompatLoadingForDrawables")
                    @Override
                    public void onSuccess(Void aVoid) {
                        revertButton();
                        Helpers.showSuccessWithActivityClose(OTPVerification.this, Constants.REGISTRATION_COMPLETED, Constants.REGISTRATION_SUCCESS);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @SuppressLint("UseCompatLoadingForDrawables")
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        revertButton();
                        Helpers.showError(OTPVerification.this, Constants.REGISTRATION_FAILED, Constants.SOMETHING_WENT_WRONG);
                    }
                });
    }


    // Exit Confirmation Alert
    public void exitConfirmation(String title, String message) {
        new FancyAlertDialog.Builder(OTPVerification.this)
                .setTitle(title)
                .setMessage(message)
                .setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark))
                .setNegativeBtnText("NO")
                .setNegativeBtnBackground(getResources().getColor(R.color.colorDanger))
                .setPositiveBtnText("YES!")
                .setPositiveBtnBackground(getResources().getColor(R.color.colorPrimary))
                .setAnimation(Animation.POP)
                .isCancellable(true)
                .setIcon(R.drawable.ic_action_okay, Icon.Visible)
                .OnPositiveClicked(new FancyAlertDialogListener() {
                    @Override
                    public void OnClick() {
                        finish();
                    }
                })
                .OnNegativeClicked(new FancyAlertDialogListener() {
                    @Override
                    public void OnClick() {
                    }
                })
                .build();
    }

    @Override
    public void onBackPressed() {
        exitConfirmation(Constants.EXIT_CONFIRMATION, Constants.EXIT_MESSAGE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                exitConfirmation(Constants.EXIT_CONFIRMATION, Constants.EXIT_MESSAGE);
                break;
            }
        }
        return true;
    }
}