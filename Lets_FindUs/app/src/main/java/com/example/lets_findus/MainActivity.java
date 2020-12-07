package com.example.lets_findus;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.net.ConnectException;

public class MainActivity extends AppCompatActivity {

    private static final int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    BluetoothManager mBluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    Button Scan_Button;
    /*private final Boolean isLocationPermissionGranted = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);*/

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


        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = mBluetoothManager.getAdapter();
        Scan_Button = findViewById(R.id.scan_button);

        Scan_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startBleScan();
            }

        });
    }

    @Override
    public void onResume(){
        super.onResume();
        if(bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
                promptEnableBluetooth();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENABLE_BLUETOOTH_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                promptEnableBluetooth();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startBleScan();
                }
                else {
                    requestLocationPermission();
                }
        }
    }



    private void promptEnableBluetooth() {
        if(!bluetoothAdapter.isEnabled()) {
            Intent enableByIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableByIntent, ENABLE_BLUETOOTH_REQUEST_CODE);
        }
    }

    private void startBleScan() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasPermission( Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestLocationPermission();
        }
        else {
            /* TODO: Actually perform scan */
        }
    }

    private void requestLocationPermission() {
        if(hasPermission( Manifest.permission.ACCESS_FINE_LOCATION))
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Location permission required")
                        .setMessage("Starting from Android M (6.0), the system requires apps to be granted " +
                                "location access in order to scan for BLE devices.")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
                            }
                        }).show();
                /* TODO: Implements .setNegativeButton */
            }
        });
    }


    private Boolean hasPermission(String accessFineLocation) {
        return ContextCompat.checkSelfPermission(MainActivity.this, accessFineLocation) == PackageManager.PERMISSION_GRANTED;
    }
}


