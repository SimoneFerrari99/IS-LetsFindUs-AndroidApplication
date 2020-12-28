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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;
    private ArrayList<ScanResult> scanResults;
    private ScanResultAdapter scanResultAdapter;
    private BluetoothGatt mGatt;
    private static RecyclerView.Adapter adapter;
    private static RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    final String NameBLE = "lets_findus";
    private static final int GATT_MAX_MTU_SIZE = 517;

    BluetoothManager BluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bleScanner;
    ScanSettings scanSettings;
    boolean isScanning;

    private final static String TAG = MainActivity.class.getSimpleName();
    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";



    Button Scan_Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        isScanning = false;


        recyclerView = findViewById(R.id.scan_results_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        scanResults = new ArrayList<>();
        scanResultAdapter = new ScanResultAdapter(scanResults);
        adapter = new ScanResultAdapter(scanResults);
        recyclerView.setAdapter(adapter);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_LONG).show();
            finish();
        }


        BluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = BluetoothManager.getAdapter();
        if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            bleScanner = bluetoothAdapter.getBluetoothLeScanner();
            bluetoothAdapter.setName(NameBLE);
            System.out.println("---------------------------SONO DENTRO ALL'IF(bluetoothAdapter != null && bluetoothAdapter.isEnabled())-------------------------");
        }


        scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build();



        Scan_Button = findViewById(R.id.scan_button);
        Scan_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isScanning) {
                    stopBleScan();
                    System.out.println("---------------------------SONO DENTRO ALL'IF(isScanning) 1°-------------------------" + isScanning);
                }
                else {
                    startBleScan();
                    System.out.println("---------------------------SONO DENTRO ALL'IF(isScanning) 2°-------------------------" + isScanning);
                }
            }

        });
        if(isScanning) {
            Scan_Button.setText("Stop Scan");
            System.out.println("---------------------------SONO DENTRO ALL'IF(Scan_Button.setText(Stop Scan))-------------------------");
        }
        else {
            Scan_Button.setText("Start Scan");
            System.out.println("---------------------------SONO DENTRO ALL'IF(Scan_Button.setText(Start Scan))-------------------------");
        }
    }


    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (bluetoothAdapter == null || mGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mGatt.readCharacteristic(characteristic);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            System.out.println("SONO DENTRO---------------------------------------------------------------------------------------------------");
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }



    public void connectToDevice(BluetoothDevice device) {
        System.out.println("BLE// connectToDevice()");
        Toast.makeText(this, "Connecting to " + device.getAddress(), Toast.LENGTH_LONG).show();
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback); //Connect to a GATT Server
            //scanLeDevice(false);// will stop after first device detection
        }
        else {
            disconnectGattServer();
            mGatt = device.connectGatt(this, false, gattCallback); //Connect to a GATT Server
        }
    }

    public void disconnectGattServer() {
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, final int status, int newState) {
            String intentAction;
            System.out.println("BLE// BluetoothGattCallback");
            Log.e("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    intentAction = ACTION_GATT_CONNECTED;
                    Log.e("gattCallback", "STATE_CONNECTED");
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            boolean ans = mGatt.discoverServices();
                            Log.d("onConnectionStateChange", "Discover Services started: " + ans);
                            mGatt.requestMtu(GATT_MAX_MTU_SIZE);

                        }
                    });
                    broadcastUpdate(intentAction);
                    break;
                case BluetoothProfile.STATE_CONNECTING:
                    Log.e("gattCallback", "STATE_CONNECTING");
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    intentAction = ACTION_GATT_DISCONNECTED;
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    broadcastUpdate(intentAction);
                    disconnectGattServer();
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
                    disconnectGattServer();
            }
        }

        @Override
        //New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.e("onServicesDiscovered", services.toString());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        //Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    @Override
    public void onResume(){
        super.onResume();
        if(bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
                promptEnableBluetooth();
        }
        requestLocationPermission();
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
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestLocationPermission();
        }
        else {
            System.out.println("---------------------------scanResults-------------------------" + scanResults);
            System.out.println("---------------------------scanResultAdapter-------------------------" + scanResultAdapter);
            if(bleScanner != null && scanResults != null && scanResultAdapter != null) {
                scanResults.clear();
                scanResultAdapter.notifyDataSetChanged();
                bleScanner.startScan(null, scanSettings, scanCallback);
                System.out.println("--------------------> startBleScan(ricerca) <--------------------");
                isScanning = true;
                Scan_Button.setText("Stop Scan");
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


    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            onBatchScanResults(scanResults);
            int indexQuery = scanResults.indexOf(result);
            if(indexQuery != -1) {
                scanResults.add(result);
                System.out.println("---------------------------scanResults 1°-------------------------" + scanResults);
                scanResultAdapter.notifyDataSetChanged();
                BluetoothDevice btDevice = result.getDevice();
                connectToDevice(btDevice);
            }
            else {
                BluetoothDevice btDevice = result.getDevice();
                Log.i("ScanCallback", "Found BLE device! Name: " + btDevice.getName() + ", Address: " + btDevice.getAddress() + ", RSSI: " + result.getRssi());
                System.out.println("---------------------------scanResults 2°-------------------------" + scanResults);
                stopBleScan();
                connectToDevice(btDevice);
            }
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


    private void requestLocationPermission() {
        if(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION))
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

            }
        });
    }

    private Boolean hasPermission(String accessFineLocation) {
        return ContextCompat.checkSelfPermission(MainActivity.this, accessFineLocation) == PackageManager.PERMISSION_GRANTED;
    }
}


