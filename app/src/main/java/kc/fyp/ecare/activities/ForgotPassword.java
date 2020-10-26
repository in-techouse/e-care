package kc.fyp.ecare.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import kc.fyp.ecare.R;
import kc.fyp.ecare.director.Constants;
import kc.fyp.ecare.director.Helpers;

public class ForgotPassword extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ForgotPassword";
    private EditText edtEmail;
    private CircularProgressButton action_send_recovery_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        edtEmail = findViewById(R.id.edtEmail);
        action_send_recovery_email = findViewById(R.id.action_send_recovery_email);
        action_send_recovery_email.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.action_send_recovery_email: {
                // Check Internet Connection
                if (!Helpers.isConnected(getApplicationContext())) {
                    Helpers.showError(ForgotPassword.this, Constants.PASSWORD_RECOVERY_FAILED, Constants.NO_INTERNET);
                    return;
                }
                // Check Email, is valid or not
                final String strEmail = edtEmail.getText().toString();
                if (!Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()) {
                    edtEmail.setError(Constants.EMAIL_ERROR);
                    return;
                }
                edtEmail.setError(null);
                action_send_recovery_email.startAnimation();
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.sendPasswordResetEmail(strEmail)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @SuppressLint("UseCompatLoadingForDrawables")
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.e(TAG, "Password Recovery Success");
                                action_send_recovery_email.revertAnimation();
                                action_send_recovery_email.setBackground(getResources().getDrawable(R.drawable.rounded_button));
                                Helpers.showSuccessWithActivityClose(ForgotPassword.this, Constants.PASSWORD_RECOVERY_SUCCESS, Constants.PASSWORD_RECOVERY_MESSAGE + strEmail);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @SuppressLint("UseCompatLoadingForDrawables")
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Password Recovery Failure: " + e.getMessage());
                                action_send_recovery_email.revertAnimation();
                                action_send_recovery_email.setBackground(getResources().getDrawable(R.drawable.rounded_button));
                                Helpers.showError(ForgotPassword.this, Constants.PASSWORD_RECOVERY_FAILED, e.getMessage());
                            }
                        });
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

}