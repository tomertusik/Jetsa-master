package com.example.gvy.jesta;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.gvy.jetsa.R;
import com.example.gvy.jetsa.databinding.JestasTypesScreenBinding;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/**
 * Created by Tomer on 10/08/2018.
 */

public class JestasTypesActivity extends AppCompatActivity {

    private JestasTypesScreenBinding binding;

    public enum Types{
        Rides,
        Home,
        Garden,
        Cleaning,
        Teachers,
        Assigment,
        Computers,
        Beauty
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.jestas_types_screen);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Model.instance.deleteAllJestas();
        checkPermissions();
        binding.rides.setOnClickListener(new OpenJestaTypeListener(Types.Rides));
        binding.home.setOnClickListener(new OpenJestaTypeListener(Types.Home));
        binding.garden.setOnClickListener(new OpenJestaTypeListener(Types.Garden));
        binding.cleaning.setOnClickListener(new OpenJestaTypeListener(Types.Cleaning));
        binding.teachers.setOnClickListener(new OpenJestaTypeListener(Types.Teachers));
        binding.assigment.setOnClickListener(new OpenJestaTypeListener(Types.Assigment));
        binding.computer.setOnClickListener(new OpenJestaTypeListener(Types.Computers));
        binding.beauty.setOnClickListener(new OpenJestaTypeListener(Types.Beauty));
        binding.myJestas.setOnClickListener(new MoveToMyJsetasListener());
        binding.myProfile.setOnClickListener(new ProfileListener());

    }

    private void checkPermissions() {
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private class OpenJestaTypeListener implements View.OnClickListener {

        private final Types type;

        public OpenJestaTypeListener(Types type) {
            this.type = type;
        }

        @Override
        public void onClick(View view) {
            Loading.startLoading(JestasTypesActivity.this);
            Intent intent = new Intent(JestasTypesActivity.this,TypeOfJestaActivity.class);
            intent.putExtra(TypeOfJestaActivity.CATEGORY,type);
            startActivity(intent);
        }
    }

    private class MoveToMyJsetasListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(JestasTypesActivity.this,MyJestasActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStackImmediate();
        else super.onBackPressed();
    }

    private class ProfileListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            MyProfileFragment fragment = MyProfileFragment.newInstance(new LogoutListener());
            getSupportFragmentManager().beginTransaction().add(binding.mainFrame.getId(), fragment).
                    addToBackStack("").commit();
        }
    }

    private class LogoutListener implements MyProfileFragment.MyProfileFragmentListener {
        @Override
        public void logOut() {
            getSupportFragmentManager().popBackStack();
            Loading.startLoading(JestasTypesActivity.this);
            AuthUI.getInstance()
                    .signOut(JestasTypesActivity.this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // user is now signed out
                            Loading.stopLoading();
                            finish();
                        }
                    });
        }
    }
}
