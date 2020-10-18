package kc.fyp.ecare.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hbb20.CountryCodePicker;
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

public class Login extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LOGIN";
    private CountryCodePicker ccp;
    private EditText edtPhone;
    private String strPhone;
    private CircularProgressButton action_login;
    private ValueEventListener listener;
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.USER_TABLE);
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView action_login_via_email = findViewById(R.id.action_login_via_email);
        TextView action_forgot_password = findViewById(R.id.action_forgot_password);
        TextView action_registration = findViewById(R.id.action_registration);

        action_login_via_email.setOnClickListener(this);
        action_forgot_password.setOnClickListener(this);
        action_registration.setOnClickListener(this);

        ccp = findViewById(R.id.ccp);
        edtPhone = findViewById(R.id.edtPhone);
        action_login = findViewById(R.id.action_login);

        action_login.setOnClickListener(this);

        ccp.registerCarrierNumberEditText(edtPhone);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void revertAnimation() {
        action_login.revertAnimation();
        action_login.setBackground(getResources().getDrawable(R.drawable.rounded_button));
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.action_login: {
                // Check Internet Connection
                if (!Helpers.isConnected(getApplicationContext())) {
                    Helpers.showError(Login.this, Constants.LOGIN_FAILED, Constants.NO_INTERNET);
                    return;
                }
                // Check Phone Number is valid or not
                if (!ccp.isValidFullNumber()) {
                    Log.e(TAG, Constants.PHONE_NUMBER_ERROR);
                    edtPhone.setError(Constants.PHONE_NUMBER_ERROR);
                    return;
                }
                Log.e(TAG, "Phone Number is valid");
                action_login.startAnimation();
                edtPhone.setError(null);
                strPhone = ccp.getFullNumberWithPlus();
                // Check user from Database
                checkUserFromDatabase();
                break;
            }
            case R.id.action_login_via_email: {
                Intent it = new Intent(Login.this, LoginViaEmail.class);
                startActivity(it);
                break;
            }
            case R.id.action_forgot_password: {
                Intent it = new Intent(Login.this, ForgotPassword.class);
                startActivity(it);
                break;
            }
            case R.id.action_registration: {
                Intent it = new Intent(Login.this, Registration.class);
                startActivity(it);
                break;
            }
        }
    }

    private void checkUserFromDatabase() {
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e(TAG, "Listener Success: " + snapshot.toString());
                if (snapshot.exists() && snapshot.getValue() != null) {
                    for (DataSnapshot d : snapshot.getChildren()) {
                        user = d.getValue(User.class);
                    }
                    if (user != null) {
                        // Send OTP to user
                        sendOtp();
                    } else {
                        revertAnimation();
                        Helpers.showError(Login.this, Constants.LOGIN_FAILED, Constants.NO_ACCOUNT_FOUND);
                    }
                } else {
                    revertAnimation();
                    Helpers.showError(Login.this, Constants.LOGIN_FAILED, Constants.NO_ACCOUNT_FOUND);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Listener Failure: " + error.getMessage());
                revertAnimation();
                Helpers.showError(Login.this, Constants.LOGIN_FAILED, Constants.SOMETHING_WENT_WRONG);
            }
        };
        reference.orderByChild("phoneNumber").equalTo(strPhone).addValueEventListener(listener);
    }

    // Send Verification OTP
    private void sendOtp() {
        PhoneAuthProvider provider = PhoneAuthProvider.getInstance();
        PhoneAuthProvider.OnVerificationStateChangedCallbacks callBack;
        callBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);
                revertAnimation();
                // Show Sent OTP Success Message
                showOTPSuccess(Constants.PHONE_NUMBER_VERIFICATION, Constants.OTP_SENT + user.getPhoneNumber(), verificationId, forceResendingToken);
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                // SignIn User Automatically
                signInUser(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                revertAnimation();
                Helpers.showError(Login.this, Constants.LOGIN_FAILED, e.getMessage());
            }
        };
        provider.verifyPhoneNumber(user.getPhoneNumber(), 120, TimeUnit.SECONDS, this, callBack);
    }

    // Show Sent OTP Success Message
    public void showOTPSuccess(String title, String message, final String verificationId, final PhoneAuthProvider.ForceResendingToken forceResendingToken) {
        new FancyAlertDialog.Builder(Login.this)
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
                        Intent it = new Intent(Login.this, OTPVerification.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("user", user);
                        bundle.putBoolean("isRegistration", false);
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
                        Intent it = new Intent(Login.this, OTPVerification.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("user", user);
                        bundle.putBoolean("isRegistration", false);
                        bundle.putString("verificationId", verificationId);
                        bundle.putParcelable("resendToken", forceResendingToken);
                        it.putExtras(bundle);
                        startActivity(it);
                        finish();
                    }
                })
                .build();
    }

    // SignIn User Automatically
    private void signInUser(PhoneAuthCredential phoneAuthCredential) {
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithCredential(phoneAuthCredential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        if (auth.getCurrentUser() != null) {
                            if (auth.getCurrentUser().isEmailVerified()) {
                                // Set Session and move to Dashboard
                                Session session = new Session(getApplicationContext());
                                session.setSession(user);
                                Log.e(TAG, "User Login is Successful");
                                Helpers.showSuccess(Login.this, "LOGIN SUCCESS!", "Your login is successfull");
                            } else {
                                revertAnimation();
                                Helpers.showError(Login.this, Constants.LOGIN_FAILED, Constants.EMAIL_NOT_VERIFIED_ERROR);
                                auth.signOut();
                            }

                        } else {
                            revertAnimation();
                            Helpers.showError(Login.this, Constants.LOGIN_FAILED, Constants.SOMETHING_WENT_WRONG);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        revertAnimation();
                        Helpers.showError(Login.this, Constants.LOGIN_FAILED, e.getMessage());
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            reference.orderByChild("phoneNumber").equalTo(strPhone).removeEventListener(listener);
        }
        if (listener != null) {
            reference.removeEventListener(listener);
        }
    }
}