package com.example.gvy.jesta;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.example.gvy.jetsa.R;
import com.example.gvy.jetsa.databinding.TypeOfJestaBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Tomer on 11/08/2018.
 */

public class TypeOfJestaActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String CATEGORY = "cat";
    private TypeOfJestaBinding binding;
    private JestasTypesActivity.Types type;
    private boolean mapShown;
    private GoogleMap googleMap;
    private SupportMapFragment mapFragment;
    public List<Jesta> jestasList;
    public List<Jesta> sortedJestasList;
    private HashMap<String,Jesta> jestaHashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, com.example.gvy.jetsa.R.layout.type_of_jesta);
    }

    @Override
    protected void onStart() {
        super.onStart();
        type = (JestasTypesActivity.Types) getIntent().getExtras().getSerializable(CATEGORY);
        binding.subtitle.setText(type.toString());
        binding.back.setOnClickListener(new BackListener());
        binding.map.setOnClickListener(new MapClickListener());
        Loading.startLoading(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
        initJestas();
    }

    private void initJestas() {
        binding.reycler.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        binding.reycler.setLayoutManager(new LinearLayoutManager(TypeOfJestaActivity.this) {
            @Override
            public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
            }
        });
        JestaViewModel viewModel = ViewModelProviders.of(this).get(JestaViewModel.class);
        viewModel.getAllJestas().observe(this, new Observer<List<Jesta>>() {
            @Override
            public void onChanged(@Nullable final List<Jesta> jestas) {
                // Update the cached copy of the words in the adapter.
                if (jestas != null) {
                    if (!jestas.isEmpty()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JestasAdapter adapter = new JestasAdapter(jestas, TypeOfJestaActivity.this, new JestaClickListener(), type);
                                binding.reycler.setAdapter(adapter);
                                jestasList = jestas;
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.reycler.setAdapter(null);
                                Loading.stopLoading();
                            }
                        });
                    }
                }
            }
        });
    }

    private void setMap() {
        if (sortedJestasList != null && googleMap != null) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                LatLng placeLocation = null;
                String cityName = "";
                for (Jesta jesta : sortedJestasList) {
                    List<Address> adresses = geocoder.getFromLocationName(jesta.getAdress(), 1);
                    if (adresses != null){
                        if (adresses.size() > 0){
                            Address address = adresses.get(0);
                            cityName = address.getAddressLine(0);
                            placeLocation = new LatLng(address.getLatitude(), address.getLongitude());
                            if (placeLocation != null) {
                                MarkerOptions options = new MarkerOptions().position(placeLocation).title(cityName + " " + jesta.getDesc());
                                Marker marker = googleMap.addMarker(options);
                                jestaHashMap.put(marker.getId(),jesta);
                                googleMap.moveCamera(CameraUpdateFactory.newLatLng(placeLocation));
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Loading.stopLoading();
        }
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        try {
            googleMap.setMyLocationEnabled(true);
        } catch (SecurityException se) {

        }
        googleMap.setTrafficEnabled(true);
        googleMap.setIndoorEnabled(true);
        googleMap.setBuildingsEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setOnMarkerClickListener(new MarkerListener());
        setMap();
    }

    private class JestaClickListener implements JestasAdapter.JestasAdapterListener {

        @Override
        public void onJestaClick(Jesta jesta, Drawable image) {
            JestaOpenFragment fragment = JestaOpenFragment.newInstance(jesta, image, false, null);
            getSupportFragmentManager().beginTransaction().add(binding.mainFrame.getId(), fragment).
                    addToBackStack("Expand").commit();
        }

        @Override
        public void setJestasList(List<Jesta> jestas) {
            sortedJestasList = jestas;
            setMap();
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStackImmediate();
        else super.onBackPressed();
    }

    private class BackListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            finish();
        }
    }

    private class MapClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (mapShown) {
                mapShown = false;
                binding.map.setText("Show In Map");
                binding.mapFrame.setVisibility(View.GONE);
            } else {
                mapShown = true;
                binding.map.setText("Show In List");
                binding.mapFrame.setVisibility(View.VISIBLE);
            }
        }
    }

    private class MarkerListener implements GoogleMap.OnMarkerClickListener {
        @Override
        public boolean onMarkerClick(Marker marker) {
            Jesta jesta = jestaHashMap.get(marker.getId());
            JestaOpenFragment fragment = JestaOpenFragment.newInstance(jesta, null, false, new LoadingListener());
            getSupportFragmentManager().beginTransaction().add(binding.mainFrame.getId(), fragment).
                    addToBackStack("Expand").commit();
            return true;
        }
    }

    private class LoadingListener implements JestaOpenFragment.ExpandDeleteListener {
        @Override
        public void onDelete() {
        }

        @Override
        public void stopLoading() {
            Loading.stopLoading();
        }

        @Override
        public void startLoading() {
            Loading.startLoading(TypeOfJestaActivity.this);
        }
    }
}
