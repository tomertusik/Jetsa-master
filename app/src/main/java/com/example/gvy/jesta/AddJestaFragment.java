package com.example.gvy.jesta;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.gvy.jetsa.R;
import com.example.gvy.jetsa.databinding.AddJestaBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Tomer on 11/08/2018.
 */

public class AddJestaFragment extends Fragment {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private AddJestaBinding binding;
    private Uri selectedImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.add_jesta, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayList<String> options = new ArrayList<>();
        for (JestasTypesActivity.Types type : JestasTypesActivity.Types.values()){
            options.add(type.toString());
        }
        String[] items = new String[options.size()];
        items = options.toArray(items);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, items);
        binding.spinner.setAdapter(adapter);
        binding.Image.setOnClickListener(new OpenCameraListener());
        binding.back.setOnClickListener(new ExitListener());
        binding.addJesta.setOnClickListener(new DoneListener());
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
            selectedImage = data.getData();
            binding.Image.setImageURI(selectedImage);
        }
    }

    private class ExitListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            closeFragment();
        }
    }

    private void closeFragment(){
        getActivity().getSupportFragmentManager().popBackStack();
    }

    private class DoneListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            JestasTypesActivity.Types category = JestasTypesActivity.Types.values()[binding.spinner.getSelectedItemPosition()];
            String desc = binding.descriptionEdit.getText().toString();
            String adress = binding.adressEdit.getText().toString();
            String price = binding.priceEdit.getText().toString();

            if (desc.isEmpty()) {
                binding.error.setVisibility(View.VISIBLE);
                return;
            }
            if (adress.isEmpty()) {
                binding.error.setVisibility(View.VISIBLE);
                return;
            }
            if (price.isEmpty()) {
                binding.error.setVisibility(View.VISIBLE);
                return;
            }
            if (selectedImage == null) {
                binding.error.setVisibility(View.VISIBLE);
                return;
            }
            Jesta jesta = new Jesta(FirebaseAuth.getInstance().getCurrentUser().getUid(),desc,category.toString(),
                    adress, price, Calendar.getInstance().getTime().getTime());
            Model.instance.AddNewJesta(jesta, selectedImage);
            Loading.startLoading(getContext());
        }
    }
}
