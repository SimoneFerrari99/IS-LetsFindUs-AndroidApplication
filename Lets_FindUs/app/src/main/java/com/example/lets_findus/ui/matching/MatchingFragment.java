package com.example.lets_findus.ui.matching;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.lets_findus.R;
import com.example.lets_findus.ui.MissingPermissionDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.gms.tasks.OnSuccessListener;

public class MatchingFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener {

    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationClient;
    private GoogleMap map;
    private View root;
    private Button show_match;

    private ActivityResultLauncher<String> requestPermissionLauncher;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        root = inflater.inflate(R.layout.fragment_matching, container, false);

        show_match = root.findViewById(R.id.matching_button);
        show_match.setVisibility(View.GONE);

        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onActivityResult(Boolean isGranted) {
                        if (isGranted) {
                            mapFragment.getMapAsync(MatchingFragment.this);
                        } else {
                            DialogFragment newFragment = new MissingPermissionDialog();
                            newFragment.show(getParentFragmentManager(), "missing_permission");
                        }
                    }
                });

        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        }
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance(new GoogleMapOptions().minZoomPreference(0f));
            mapFragment.getMapAsync(this);
        }
        // R.id.map is a FrameLayout, not a Fragment
        getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        //prova per bottone di ricerca
        show_match.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Ciao more", Toast.LENGTH_SHORT).show();
                show_match.setVisibility(View.GONE);
                VisibleRegion vr = map.getProjection().getVisibleRegion();
                map.addMarker(new MarkerOptions()
                        .position(vr.farLeft)
                        .title("far left"));
                map.addMarker(new MarkerOptions()
                        .position(vr.farRight)
                        .title("far right"));
                map.addMarker(new MarkerOptions()
                        .position(vr.nearLeft)
                        .title("near left"));
                map.addMarker(new MarkerOptions()
                        .position(vr.nearRight)
                        .title("near right"));
            }
        });


        return root;
    }

    @SuppressLint("MissingPermission")
    private void setupMap(final GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, null).addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f));
                    }
                }
            });
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        map = googleMap;
        setupMap(map);
        map.setOnCameraMoveStartedListener(this);
    }

    @Override
    public void onCameraMoveStarted(int i) {
        if(i == REASON_GESTURE)
            show_match.setVisibility(View.VISIBLE);
    }
}