package com.example.lets_findus;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private ArrayList<ScanResult> scanResults;
    private ScanResultAdapter scanResultAdapter;
    private BluetoothGatt mGatt;
    private static RecyclerView.Adapter adapter;
    private static RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;

    BluetoothManager BluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bleScanner;
    ScanSettings scanSettings;
    boolean isScanning = false;





    Button Scan_Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        recyclerView = findViewById(R.id.scan_results_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        scanResults = new ArrayList<>();
        adapter = new ScanResultAdapter(scanResults);
        recyclerView.setAdapter(adapter);


        BluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = BluetoothManager.getAdapter();
        if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();
            System.out.println("---------------------------SONO DENTRO ALL'IF-------------------------");
        }

        scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();


        Scan_Button = findViewById(R.id.scan_button);
        Scan_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isScanning) {
                    stopBleScan();
                }
                else {
                    startBleScan();
                }
            }

        });
        if(isScanning) {
            Scan_Button.setText("Stop Scan");
        }
        else {
            Scan_Button.setText("Start Scan");
        }
    }

    

    ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            int indexQuery = scanResults.indexOf(result);
            if(indexQuery != -1) {
                scanResults.add(result);
                scanResultAdapter.notifyItemChanged(scanResults.size() - 1);
            }
            else {
                BluetoothDevice btDevice = result.getDevice();
                Log.i("ScanCallback", "Found BLE device! Name: " + btDevice.getName() + ", Address: " + btDevice.getAddress());
            }
            scanResults.add(result);
            scanResultAdapter.notifyItemInserted(scanResults.size() - 1);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for(ScanResult sr : results) {
                Log.i("ScanResult", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    public void connectToDevice(BluetoothDevice device) {
        System.out.println("BLE// connectToDevice()");
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback); //Connect to a GATT Server
            //scanLeDevice(false);// will stop after first device detection
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            System.out.println("BLE// BluetoothGattCallback");
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_CONNECTING:
                    Log.i("gattCallback", "STATE_CONNECTING");
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }

        @Override
        //New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());
            gatt.readCharacteristic(services.get(1).getCharacteristics().get(0));
        }

        @Override
        //Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            gatt.disconnect();
        }
    };

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
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startBleScan();
            } else {
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
            if(bleScanner != null && scanResults != null && scanResultAdapter != null) {
                scanResults.clear();
                scanResultAdapter.notifyDataSetChanged();
                bleScanner.startScan(null, scanSettings, scanCallback);
                System.out.println("--------------------> bleScanner: SONO ENTRATO NELL'IF <--------------------");
                isScanning = true;
                Scan_Button.setText("Stop Scan");
                System.out.println("--------------------> isScanning: <--------------------" + isScanning);
            }
        }
    }

    private void stopBleScan() {
        if(bleScanner != null) {
            bleScanner.stopScan(scanCallback);
            isScanning = false;
            Scan_Button.setText("Start Scan");
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


