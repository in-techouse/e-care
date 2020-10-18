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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import kc.fyp.ecare.R;
import kc.fyp.ecare.director.Constants;
import kc.fyp.ecare.director.Helpers;

public class LoginViaEmail extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoginViaEmail";
    private EditText edtEmail, edtPassword;
    private String strEmail, strPassword;
    private CircularProgressButton action_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_via_email);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        action_login = findViewById(R.id.action_login);
        action_login.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.action_login: {
                // Check Internet Connection
                if (!Helpers.isConnected(getApplicationContext())) {
                    Helpers.showError(LoginViaEmail.this, Constants.LOGIN_FAILED, Constants.NO_INTERNET);
                    return;
                }
                // Check if data is valid or not
                boolean flag = isValid();
                if (flag) {
                    action_login.startAnimation();
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    auth.signInWithEmailAndPassword(strEmail, strPassword)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @SuppressLint("UseCompatLoadingForDrawables")
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    Log.e(TAG, "Login Success");
                                    action_login.revertAnimation();
                                    action_login.setBackground(getResources().getDrawable(R.drawable.rounded_button));
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @SuppressLint("UseCompatLoadingForDrawables")
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Login Failure: " + e.getMessage());
                                    action_login.revertAnimation();
                                    action_login.setBackground(getResources().getDrawable(R.drawable.rounded_button));
                                    Helpers.showError(LoginViaEmail.this, Constants.LOGIN_FAILED, e.getMessage());
                                }
                            });
                }
                break;
            }
        }
    }

    private boolean isValid() {
        boolean flag = true;
        strEmail = edtEmail.getText().toString();
        strPassword = edtPassword.getText().toString();
        if (!Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()) {
            edtEmail.setError(Constants.EMAIL_ERROR);
            flag = false;
        } else {
            edtEmail.setError(null);
        }

        if (strPassword.length() < 6) {
            edtPassword.setError(Constants.PASSWORD_ERROR);
            flag = false;
        } else {
            edtPassword.setError(null);
        }

        return flag;
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