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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import kc.fyp.ecare.R;
import kc.fyp.ecare.director.Constants;
import kc.fyp.ecare.director.Helpers;
import kc.fyp.ecare.director.Session;
import kc.fyp.ecare.models.User;

public class LoginViaEmail extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoginViaEmail";
    private EditText edtEmail, edtPassword;
    private String strEmail, strPassword;
    private CircularProgressButton action_login;
    private DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.USER_TABLE);
    private User user;
    private ValueEventListener listener;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_via_email);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        action_login = findViewById(R.id.action_login);
        action_login.setOnClickListener(this);
    }

    private void disableInput() {
        action_login.startAnimation();
        edtEmail.setEnabled(false);
        edtPassword.setEnabled(false);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void enableInput() {
        action_login.revertAnimation();
        action_login.setBackground(getResources().getDrawable(R.drawable.rounded_button));
        edtEmail.setEnabled(true);
        edtPassword.setEnabled(true);
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
                    disableInput();
                    auth.signInWithEmailAndPassword(strEmail, strPassword)
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    Log.e(TAG, "Login Success");
                                    if (auth.getCurrentUser() != null) {
                                        if (auth.getCurrentUser().isEmailVerified()) {
                                            Log.e(TAG, "User Login is Successful");
                                            // Get user detail from database
                                            getUserDetailFromDatabase();
                                        } else {
                                            enableInput();
                                            Helpers.showError(LoginViaEmail.this, Constants.LOGIN_FAILED, Constants.EMAIL_NOT_VERIFIED_ERROR);
                                            auth.signOut();
                                        }
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "Login Failure: " + e.getMessage());
                                    enableInput();
                                    Helpers.showError(LoginViaEmail.this, Constants.LOGIN_FAILED, e.getMessage());
                                }
                            });
                }
                break;
            }
        }
    }

    private void getUserDetailFromDatabase() {
        if (auth.getCurrentUser() != null) {
            listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.getValue() != null) {
                        user = snapshot.getValue(User.class);
                        // Set Session and move to Dashboard
                        Session session = new Session(getApplicationContext());
                        user.setVerified(true);
                        session.setSession(user);
                        Log.e(TAG, "User Login is Successful");
                        Helpers.showSuccess(LoginViaEmail.this, "LOGIN SUCCESS!", "Your login is successfull");
                    } else {
                        enableInput();
                        auth.signOut();
                        Helpers.showError(LoginViaEmail.this, Constants.LOGIN_FAILED, Constants.SOMETHING_WENT_WRONG);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    enableInput();
                    auth.signOut();
                    Helpers.showError(LoginViaEmail.this, Constants.LOGIN_FAILED, Constants.SOMETHING_WENT_WRONG);
                }
            };
            reference.child(auth.getCurrentUser().getUid()).addValueEventListener(listener);
        } else {
            enableInput();
            auth.signOut();
            Helpers.showError(LoginViaEmail.this, Constants.LOGIN_FAILED, Constants.SOMETHING_WENT_WRONG);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) {
            reference.removeEventListener(listener);
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