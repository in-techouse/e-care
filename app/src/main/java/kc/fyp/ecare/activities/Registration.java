package kc.fyp.ecare.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.hbb20.CountryCodePicker;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import kc.fyp.ecare.R;
import kc.fyp.ecare.director.Constants;
import kc.fyp.ecare.director.Helpers;

public class Registration extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Registration";
    private EditText edtName, edtEmail, edtPhone, edtPassword, edtPasswordConfirmation;
    private String strName, strEmail, strPhone, strPassword, strPasswordConfirmation;
    private CountryCodePicker ccp;
    private CircularProgressButton action_register;

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

        action_register.setOnClickListener(this);
    }

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
                boolean flag = isValid();
                if (flag) {
                    action_register.startAnimation();
                }
                break;
            }
        }
    }

    private boolean isValid() {
        boolean flag = true;
        strName = edtName.getText().toString();
        strEmail = edtEmail.getText().toString();
        strPhone = ccp.getFullNumberWithPlus();
        strPassword = edtPassword.getText().toString();
        strPasswordConfirmation = edtPasswordConfirmation.getText().toString();

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