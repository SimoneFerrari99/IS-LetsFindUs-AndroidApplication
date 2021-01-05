package com.example.lets_findus;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.lets_findus.GattAttributes.SERVICE_UUID;
import static com.example.lets_findus.Utilis.findCharacteristics;


public class ClientActivity extends AppCompatActivity {

    EditText mEdit;

    private boolean mConnected;
    private boolean mTimeInitialized;
    private boolean mEchoInitialized;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanCallback mScanCallback;
    private BluetoothGatt mGatt;
    private static final long SCAN_PERIOD = 5000;
    private static BluetoothManager bluetoothManager;


    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 2;


    private boolean mScanning;
    private Handler mHandler;
    private Handler mLogHandler;
    private Map<String, BluetoothDevice> mScanResults;


    Button start;
    Button stop;
    Button disconnect;
    Button send;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        mLogHandler = new Handler(Looper.getMainLooper());

        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mEdit = findViewById(R.id.message_edit_text);
        start = findViewById(R.id.start_scanning_button);
        stop = findViewById(R.id.stop_scanning_button);
        disconnect = findViewById(R.id.disconnect_button);
        send = findViewById(R.id.send_message_button);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScan();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScan();
            }
        });

        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnectGattServer();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No BLE Support", Toast.LENGTH_LONG).show();
            finish();
        }
    }

/* ----------------------------- SEZIONE SCANSIONE ----------------------------- */

    private void startScan() {
        if (!hasPermissions() || mScanning) {
            return;
        }

        disconnectGattServer();


        mScanResults = new HashMap<>();
        mScanCallback = new FirstScanCallback(mScanResults);

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(SERVICE_UUID))
                .build();
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(scanFilter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();

        mBluetoothLeScanner.startScan(filters, settings, mScanCallback);

        mHandler = new Handler();
        mHandler.postDelayed(this::stopScan, SCAN_PERIOD);

        mScanning = true;
        Toast.makeText(this, "Started scanning", Toast.LENGTH_LONG).show();
    }

    private void stopScan() {
        if (mScanning && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(mScanCallback);
            scanComplete();
        }

        mScanCallback = null;
        mScanning = false;
        mHandler = null;
        Toast.makeText(this, "Stopped scanning", Toast.LENGTH_LONG).show();
    }

    private void scanComplete() {
        if (mScanResults.isEmpty()) {
            return;
        }
        else {
            for (String deviceAddress : mScanResults.keySet()) {
                BluetoothDevice device = mScanResults.get(deviceAddress);
                connectDevice(device);
            }
        }
    }

/* ------------------------------------------------------------------------------ */

/* ----------------------------- RICHIESTA PERMESSI ----------------------------- */

    private boolean hasPermissions() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            requestBluetoothEnable();
            return false;
        } else if (!hasLocationPermissions()) {
            requestLocationPermission();
            return false;
        }
        return true;
    }

    private boolean hasLocationPermissions() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
        Toast.makeText(this, "Requested user enable Location. Try starting the scan again", Toast.LENGTH_LONG).show();
    }

    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        Toast.makeText(this, "Requested user enables Bluetooth. Try starting the scan again", Toast.LENGTH_LONG).show();
    }

/* ------------------------------------------------------------------------------ */

/* ----------------------------- CONNESSIONE AL DEVICE ----------------------------- */

    private void connectDevice(BluetoothDevice device) {
        Toast.makeText(this, "Connecting to " + device.getName(), Toast.LENGTH_LONG).show();
        mGatt = device.connectGatt(this, false, gattCallback);
    }

/* ------------------------------------------------------------------------------ */

/* ----------------------------- GATT CALLBACK ----------------------------- */

    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("onConnectionStateChange", "STATE_CONNECTED");
                    Log.i("onConnectionStateChange", "Connected to device " + gatt.getDevice().getName());
                    setConnected(true);
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i("onConnectionStateChange", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.i("onConnectionStateChange", "newState = " + newState);
                    disconnectGattServer();
                    break;
            }
            switch (status) {
                case BluetoothGatt.GATT_FAILURE:
                    Log.i("onConnectionStateChange", "Connection Gatt failure status " + status);
                    break;
                default:
                    Log.i("onConnectionStateChange", "Connection not GATT sucess status " + status);
                    break;

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            List<BluetoothGattCharacteristic> matchingCharacteristics = findCharacteristics(gatt);

            if (matchingCharacteristics.isEmpty()) {
                Log.i("onServicesDiscovered", "Unable to find characteristics");
                return;
            }
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.i("onServicesDiscovered", "Device service discovery unsuccessful, status " + status);
                return;
            }
            Log.i("onServicesDiscovered", "Initializing: setting write type and enabling notification");
            for (BluetoothGattCharacteristic characteristic : matchingCharacteristics) {
                characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                boolean characteristicWriteSuccess = gatt.setCharacteristicNotification(characteristic, true);
                if (characteristicWriteSuccess) {
                    Log.i("onServicesDiscovered", "Characteristic notification set successfully for " + characteristic.getUuid().toString());
                    if (Utilis.isEchoCharacteristic(characteristic)) {
                        initializeEcho();
                    } else if (Utilis.isTimeCharacteristic(characteristic)) {
                        List<BluetoothGattDescriptor> descriptorList = characteristic.getDescriptors();
                        BluetoothGattDescriptor descriptor = Utilis.findClientConfigurationDescriptor(descriptorList);
                        if (descriptor == null) {
                            Log.i("onServicesDiscovered", "Unable to find Characteristic Configuration Descriptor");
                            return;
                        }

                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        boolean descriptorWriteInitiated = gatt.writeDescriptor(descriptor);
                        if (descriptorWriteInitiated) {
                            Log.i("onServicesDiscovered", "Characteristic Configuration Descriptor write initiated: " + descriptor.getUuid().toString());
                        } else {
                            Log.e("onServicesDiscovered", "Characteristic Configuration Descriptor write failed to initiate: " + descriptor.getUuid().toString());
                        }
                    }
                } else {
                    Log.i("onServicesDiscovered", "Characteristic notification set failure for " + characteristic.getUuid().toString());
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("onCharacteristicRead", "Characteristic read successfully");
                byte[] messageBytes = characteristic.getValue();
                Log.i("onCharacteristicRead", "Read: " + Utilis.byteArrayInHexFormat(messageBytes));
                String message = Utilis.stringFromBytes(messageBytes);
                if (message == null) {
                    Log.e("onCharacteristicRead", "Unable to convert bytes to string");
                    return;
                }

                Log.e("onCharacteristicRead", "Received message: " + message);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("onCharacteristicWrite", "Characteristic written successfully");
            } else {
                Log.i("onCharacteristicWrite", "Characteristic write unsuccessful, status: " + status);
                disconnectGattServer();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i("onCharacteristicChanged", "Characteristic changed, " + characteristic.getUuid().toString());
            byte[] messageBytes = characteristic.getValue();
            Log.i("onCharacteristicChanged", "Read: " + Utilis.byteArrayInHexFormat(messageBytes));
            String message = Utilis.stringFromBytes(messageBytes);
            if (message == null) {
                Log.e("onCharacteristicChanged", "Unable to convert bytes to string");
                return;
            }

            Log.e("onCharacteristicChanged", "Received message: " + message);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("onDescriptorWrite", "Descriptor written successfully: " + descriptor.getUuid().toString());
            } else {
                Log.i("onDescriptorWrite", "Descriptor write unsuccessful: " + descriptor.getUuid().toString());
            }
        }

    };

/* ------------------------------------------------------------------------------ */

/* ----------------------------- INVIO DEL DATO ----------------------------- */

    private void sendMessage() {
        if (!mConnected) {
            return;
        }

        BluetoothGattCharacteristic characteristic = Utilis.findEchoCharacteristic(mGatt);
        if (characteristic == null) {
            Log.e("sendMessage", "Unable to find echo characteristic");
            disconnectGattServer();
            return;
        }

        String message = mEdit.getText().toString();
        Log.i("sendMessage", "Sending message: " + message);

        byte[] messageBytes = Utilis.bytesFromString(message);
        if (messageBytes.length == 0) {
            Log.e("sendMessage", "Unable to convert message to bytes");
            return;
        }

        characteristic.setValue(messageBytes);
        boolean success = mGatt.writeCharacteristic(characteristic);
        if (success) {
            Log.i("sendMessage", "Wrote: " + Utilis.byteArrayInHexFormat(messageBytes));
        } else {
            Log.e("sendMessage", "Failed to write data");
        }
    }

/* ------------------------------------------------------------------------------ */

/* ----------------------------- UTILITY ----------------------------- */

    public void setConnected(boolean connected) {
        mConnected = connected;
    }

    public void initializeTime() {
        mTimeInitialized = true;
    }

    public void initializeEcho() {
        mEchoInitialized = true;
    }

    public void disconnectGattServer() {
        mConnected = false;
        mEchoInitialized = false;
        mTimeInitialized = false;
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
        }
    }

/* ------------------------------------------------------------------------------ */

}
