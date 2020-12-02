package com.example.lets_findus.ui.matching;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Point;
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
import androidx.fragment.app.Fragment;

import com.example.lets_findus.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MatchingFragment extends Fragment {

    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient fusedLocationClient;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_matching, container, false);

        if (fusedLocationClient == null) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        }
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
        }
        mapFragment.getMapAsync(withLocationGranted);
        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onActivityResult(Boolean isGranted) {
                        if (isGranted) {
                            mapFragment.getMapAsync(new OnMapReadyCallback(){
                                @Override
                                public void onMapReady(final GoogleMap googleMap) {
                                    fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                                        @Override
                                        public void onSuccess(Location location) {
                                            if (location != null) {
                                                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                                                googleMap.addMarker(new MarkerOptions().position(latLng)
                                                        .title("CiaoSimo"));
                                                googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                                            }
                                        }
                                    });
                                }
                            });
                        } else {
                            // Explain to the user that the feature is unavailable because the
                            // features requires a permission that the user has denied. At the
                            // same time, respect the user's decision. Don't link to system
                            // settings in an effort to convince the user to change their
                            // decision.
                        }
                    }
                });

        // R.id.map is a FrameLayout, not a Fragment
        getChildFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();

        return root;
    }

    private OnMapReadyCallback withLocationGranted = new OnMapReadyCallback() {
        @Override
        public void onMapReady(final GoogleMap googleMap) {
            int locationPermission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION);
            if (locationPermission == PERMISSION_GRANTED) {
                fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, null).addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            googleMap.addMarker(new MarkerOptions().position(latLng)
                                    .title("CiaoSimo"));
                            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                            googleMap.animateCamera(CameraUpdateFactory.zoomBy(10.0f, new Point((int)latLng.latitude, (int)latLng.longitude)));
                        }
                    }
                });
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
    };
}