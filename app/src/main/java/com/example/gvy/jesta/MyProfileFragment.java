package com.example.gvy.jesta;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gvy.jetsa.R;
import com.example.gvy.jetsa.databinding.MyProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;

/**
 * Created by Tomer on 11/08/2018.
 */

public class MyProfileFragment extends Fragment {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private MyProfileBinding binding;
    private MyProfileFragmentListener listener;

    public interface MyProfileFragmentListener{
        void logOut();
    }

    public static final MyProfileFragment newInstance(MyProfileFragmentListener listener) {
        MyProfileFragment fragment = new MyProfileFragment();
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.my_profile, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.logout.setOnClickListener(new LogoutListener());
        FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();
        Bitmap image = Model.instance.loadImageFromFile(currUser.getUid());
        if (image != null){
            binding.Image.setImageBitmap(image);
        }
        binding.name.setText(currUser.getDisplayName());
        binding.email.setText(currUser.getEmail());
        binding.Image.setOnClickListener(new OpenCameraListener());
    }

    private class LogoutListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            listener.logOut();
        }
    }

    private class OpenCameraListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent takePictureIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE &&
                resultCode == Activity.RESULT_OK) {
            Uri selectedImage = data.getData();
            binding.Image.setImageURI(selectedImage);
            Bitmap imageBitmap = null;
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(JestaApp.getContext().getContentResolver(), selectedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
            FirebaseUser currUser = FirebaseAuth.getInstance().getCurrentUser();
            Model.instance.saveImageToFile(imageBitmap,currUser.getUid());
        }
    }
}
