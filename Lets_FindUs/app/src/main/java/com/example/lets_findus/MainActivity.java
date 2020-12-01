package com.example.lets_findus;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    BluetoothAdapter bluetoothAdapter;
    Button Scan_Button = findViewById(R.id.scan_button);
    private Boolean isLocationPermissionGranted = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        Scan_Button.setOnClickListener(startBleScan());
    }

    private void promptEnableBluetooth() {
        if(!bluetoothAdapter.isEnabled()) {
            Intent enableByIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableByIntent, ENABLE_BLUETOOTH_REQUEST_CODE);
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(!bluetoothAdapter.isEnabled()) {
            promptEnableBluetooth();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ENABLE_BLUETOOTH_REQUEST_CODE :
                if(resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth();
                }
        }
    }

    private Boolean hasPermission(String accessFineLocation) {
        return ContextCompat.checkSelfPermission(this, accessFineLocation) == PackageManager.PERMISSION_GRANTED;
    }


    private void startBleScan() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission();
        }
        else {
            /* TODO: Actually perform scan */
        }
    }
    private void requestLocationPermission() {
        if(isLocationPermissionGranted)
            return;
        runOnUiThread(new Thread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog(getApplicationContext())
                        .setTitle("Location permission required")
                        .setMessage("Starting from Android M (6.0), the system requires apps to be granted " +
                                "location access in order to scan for BLE devices.")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok) {
                                requestPermissions(
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        LOCATION_PERMISSION_REQUEST_CODE
                                );
                }
            }
        }));
    }
}


