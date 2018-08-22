package com.example.gvy.jesta;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.gvy.jetsa.R;
import com.example.gvy.jetsa.databinding.LoginActivityBinding;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private static final int LOG_IN = 9999;
    private LoginActivityBinding binding;
    private boolean loggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.login_activity);
    }

    @Override
    protected void onStart() {
        super.onStart();
        JestaApp.setContext(getApplicationContext());
        checkForLogin();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                moveToLogin();
            }
        }, 3000);
    }

    private void checkForLogin() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            loggedIn = true;
        } else {
            loggedIn = false;
        }
    }

    private void moveToLogin() {
        if (loggedIn){
            moveToJestasTypes();
        } else {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.FacebookBuilder().build(),
                                    new AuthUI.IdpConfig.GoogleBuilder().build()
                            )).setTheme(R.style.LoginTheme)
                            .build(),
                    LOG_IN);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOG_IN) {
            // Successfully signed in
            if (resultCode == RESULT_OK) {
                //moveToJestasTypes();
            } else {
                // Sign in failed
                // TODO: show error message
            }
        }
    }

    private void moveToJestasTypes() {
        Intent intent = new Intent(LoginActivity.this,JestasTypesActivity.class);
        startActivity(intent);
    }
}
