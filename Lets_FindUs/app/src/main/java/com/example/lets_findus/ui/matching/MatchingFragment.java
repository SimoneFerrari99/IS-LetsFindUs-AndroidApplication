package com.example.lets_findus.ui.matching;

import android.Manifest;
import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.lets_findus.R;
import com.example.lets_findus.ui.MissingPermissionDialog;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MatchingFragment extends Fragment {

    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationClient;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @SuppressLint("MissingPermission")
    private void setupMap(final GoogleMap googleMap){
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, null).addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.setMyLocationEnabled(true);
                    googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f));
                }
            }
        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_matching, container, false);

        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        }
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            mapFragment.getMapAsync(withLocationGranted);
            requestPermissionLauncher =
                    registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onActivityResult(Boolean isGranted) {
                            if (isGranted) {
                                mapFragment.getMapAsync(new OnMapReadyCallback() {
                                    @Override
                                    public void onMapReady(final GoogleMap googleMap) {
                                       setupMap(googleMap);
                                    }
                                });
                            } else {
                                DialogFragment newFragment = new MissingPermissionDialog();
                                newFragment.show(getParentFragmentManager(), "missing_permission");
                            }
                        }
                    });
        }

        // R.id.map is a FrameLayout, not a Fragment
        getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();

        return root;
    }

    private OnMapReadyCallback withLocationGranted = new OnMapReadyCallback() {
        @Override
        public void onMapReady(final GoogleMap googleMap) {
            int locationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION);
            if (locationPermission == PERMISSION_GRANTED) {
                setupMap(googleMap);
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
    };

}