package kc.fyp.ecare.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import kc.fyp.ecare.R;
import kc.fyp.ecare.director.Helpers;

public class Login extends AppCompatActivity implements View.OnClickListener {

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
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
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