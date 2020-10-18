package kc.fyp.ecare.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hbb20.CountryCodePicker;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import kc.fyp.ecare.R;
import kc.fyp.ecare.director.Constants;
import kc.fyp.ecare.director.Helpers;

public class Login extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LOGIN";
    private CountryCodePicker ccp;
    private EditText edtPhone;
    private CircularProgressButton action_login;

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
                edtPhone.setError(null);
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
}