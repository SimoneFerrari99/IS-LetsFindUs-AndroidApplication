package com.example.lets_findus.client;

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
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lets_findus.R;
import com.example.lets_findus.Utilis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.lets_findus.GattAttributes.SERVICE_UUID;
import static com.example.lets_findus.Utilis.findCharacteristics;


public class ClientActivity extends AppCompatActivity {

    EditText mEdit;
    private Handler mLogHandler;
    private TextView log;

    private boolean mConnected;
    private boolean mTimeInitialized;
    private boolean mEchoInitialized;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothGatt mGatt;
    private static final long SCAN_PERIOD = 5000;
    private static BluetoothManager bluetoothManager;


    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 2;


    private boolean mScanning;
    private Handler mHandler;
    private Map<String, BluetoothDevice> mScanResults;

    private static final int DEFAULT_BYTES_VIA_BLE = 512;



    Button start;
    Button stop;
    Button disconnect;
    Button send;

/* ----------------------------- LIFECYCLE ----------------------------- */

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        log = findViewById(R.id.log_text_view);
        mLogHandler = new Handler(Looper.getMainLooper());
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
                String message = mEdit.getText().toString();
                byte[] messageBytes = Utilis.bytesFromString(message);
                //sendMessage();
                sendImage(messageBytes, false);
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

/* ------------------------------------------------------------------------------ */

/* ----------------------------- SEZIONE SCANSIONE ----------------------------- */

    private void startScan() {
        if (!hasPermissions() || mScanning) {
            return;
        }

        disconnectGattServer();


        mScanResults = new HashMap<>();

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        ScanFilter scanFilter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(SERVICE_UUID))
                .build();
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(scanFilter);

        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();

        mBluetoothLeScanner.startScan(filters, settings, scanCallback);

        mHandler = new Handler();
        mHandler.postDelayed(this::stopScan, SCAN_PERIOD);

        mScanning = true;
        mLogHandler.post(() -> {
            log.append("\nStarted scanning");
        });
    }

    private void stopScan() {
        if (mScanning && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(scanCallback);
            scanComplete();
        }

        scanCallback = null;
        mScanning = false;
        mHandler = null;
        mLogHandler.post(() -> {
            log.append("\nStopped scanning after 5000ms");
        });
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
        mLogHandler.post(() -> {
            log.append("\nRequested user enable Location. Try starting the scan again");
        });
    }

    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        mLogHandler.post(() -> {
            log.append("\nRequested user enable Bluetooth. Try starting the scan again");
        });
    }

/* ------------------------------------------------------------------------------ */

/* ----------------------------- CONNESSIONE AL DEVICE ----------------------------- */

    private void connectDevice(BluetoothDevice device) {
        mLogHandler.post(() -> {
            log.append("\nConnecting to " + device.getName());
        });
        mGatt = device.connectGatt(this, false, gattCallback);
    }

/* ------------------------------------------------------------------------------ */

/* ----------------------------- SCAN CALLBACK ----------------------------- */

    ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String deviceAddress = device.getAddress();
            mScanResults.put(deviceAddress, device);
            System.out.println(result.getDevice());

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                mLogHandler.post(() -> {
                    log.append("\nScanResult: " + result.toString());
                });
                BluetoothDevice device = result.getDevice();
                String deviceAddress = device.getAddress();
                mScanResults.put(deviceAddress, device);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            mLogHandler.post(() -> {
                log.append("\nScan Failed --> Error Code: " + errorCode);
            });
        }
    };

/* ------------------------------------------------------------------------------ */

/* ----------------------------- GATT CALLBACK ----------------------------- */

    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            boolean mtu;

            mLogHandler.post(() -> {
                log.append("\nonConnectionStateChange --> Status: " + status);
            });
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    mLogHandler.post(() -> {
                        log.append("\nonConnectionStateChange --> STATE_CONNECTED");
                        log.append("\nonConnectionStateChange --> Connected to device " + gatt.getDevice().getName());
                    });
                    setConnected(true);
                    mtu = gatt.requestMtu(517);
                    mLogHandler.post(() -> {
                        log.append("\nrequestMtu --> " + mtu);
                    });
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    mLogHandler.post(() -> {
                        log.append("\nonConnectionStateChange --> STATE_DISCONNECTED");
                    });
                    break;
                default:
                    mLogHandler.post(() -> {
                        log.append("\nonConnectionStateChange --> newState = " + newState);
                    });
                    disconnectGattServer();
                    break;
            }
            switch (status) {
                case BluetoothGatt.GATT_FAILURE:
                    mLogHandler.post(() -> {
                        log.append("\nonConnectionStateChange --> Connection Gatt failure status " + status);
                    });
                    break;
                default:
                    mLogHandler.post(() -> {
                        log.append("\nonConnectionStateChange --> Connection not GATT sucess status " + status);
                    });
                    break;

            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            List<BluetoothGattCharacteristic> matchingCharacteristics = findCharacteristics(gatt);

            if (matchingCharacteristics.isEmpty()) {
                mLogHandler.post(() -> {
                    log.append("\nonServicesDiscovered --> Unable to find characteristics");
                });
                return;
            }
            if (status != BluetoothGatt.GATT_SUCCESS) {
                mLogHandler.post(() -> {
                    log.append("\nonServicesDiscovered --> Device service discovery unsuccessful, status " + status);
                });
                return;
            }
            mLogHandler.post(() -> {
                log.append("\nonServicesDiscovered --> Initializing: setting write type and enabling notification");
            });
            for (BluetoothGattCharacteristic characteristic : matchingCharacteristics) {
                characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                boolean characteristicWriteSuccess = gatt.setCharacteristicNotification(characteristic, true);
                if (characteristicWriteSuccess) {
                    mLogHandler.post(() -> {
                        log.append("\nonServicesDiscovered --> Characteristic notification set successfully for " + characteristic.getUuid().toString());
                    });
                    if (Utilis.isEchoCharacteristic(characteristic)) {
                        initializeEcho();
                    } else if (Utilis.isTimeCharacteristic(characteristic)) {
                        List<BluetoothGattDescriptor> descriptorList = characteristic.getDescriptors();
                        BluetoothGattDescriptor descriptor = Utilis.findClientConfigurationDescriptor(descriptorList);
                        if (descriptor == null) {
                            mLogHandler.post(() -> {
                                log.append("\nonServicesDiscovered --> Unable to find Characteristic Configuration Descriptor");
                            });
                            return;
                        }

                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        boolean descriptorWriteInitiated = gatt.writeDescriptor(descriptor);
                        if (descriptorWriteInitiated) {
                            mLogHandler.post(() -> {
                                log.append("\nonServicesDiscovered --> Characteristic Configuration Descriptor write initiated: " + descriptor.getUuid().toString());
                            });
                        } else {
                            mLogHandler.post(() -> {
                                log.append("\nonServicesDiscovered --> Characteristic Configuration Descriptor write failed to initiate: " + descriptor.getUuid().toString());
                            });
                        }
                    }
                } else {
                    mLogHandler.post(() -> {
                        log.append("\nonServicesDiscovered --> Characteristic notification set failure for " + characteristic.getUuid().toString());
                    });
                }
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mLogHandler.post(() -> {
                    log.append("\nonCharacteristicRead --> Characteristic read successfully");
                });
                byte[] messageBytes = characteristic.getValue();
                mLogHandler.post(() -> {
                    log.append("\nonCharacteristicRead --> Read: " + Utilis.byteArrayInHexFormat(messageBytes));
                });
                String message = Utilis.stringFromBytes(messageBytes);
                if (message == null) {
                    mLogHandler.post(() -> {
                        log.append("\nonCharacteristicRead --> Unable to convert bytes to string");
                    });
                    return;
                }
                mLogHandler.post(() -> {
                    log.append("\nonCharacteristicRead --> Received message: " + message);
                });
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mLogHandler.post(() -> {
                    log.append("\nonCharacteristicWrite --> Characteristic written successfully");
                });
            } else {
                mLogHandler.post(() -> {
                    log.append("\nonCharacteristicWrite --> Characteristic write unsuccessful, status: " + status);
                });
                disconnectGattServer();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            mLogHandler.post(() -> {
                log.append("\nonCharacteristicChanged --> Characteristic changed, " + characteristic.getUuid().toString());
            });
            byte[] messageBytes = characteristic.getValue();
            mLogHandler.post(() -> {
                log.append("\nonCharacteristicChanged --> Read " + Utilis.byteArrayInHexFormat(messageBytes));
            });
            String message = Utilis.stringFromBytes(messageBytes);
            if (message == null) {
                mLogHandler.post(() -> {
                    log.append("\nonCharacteristicChanged --> Unable to convert bytes to string");
                });
                return;
            }
            mLogHandler.post(() -> {
                log.append("\nonCharacteristicChanged --> Received message: " + message);
            });
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mLogHandler.post(() -> {
                    log.append("\nonDescriptorWrite --> Descriptor written successfully: " + descriptor.getUuid().toString());
                });
            } else {
                mLogHandler.post(() -> {
                    log.append("\nonDescriptorWrite --> Descriptor write unsuccessful: " + descriptor.getUuid().toString());
                });
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            if(status == 0) {
                mLogHandler.post(() -> {
                    log.append("\nonMtuChanged --> Mtu changed successfully: " + mtu + ", " + status);
                    gatt.discoverServices();
                });
            }
            else {
                mLogHandler.post(() -> {
                    log.append("\nonMtuChanged --> Mtu not changed (Mtu not supported): " + mtu + ", " + status);
                    gatt.discoverServices();
                });
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
            mLogHandler.post(() -> {
                log.append("\nsendMessage --> Unable to find echo characteristic");
            });
            disconnectGattServer();
            return;
        }

        String message = mEdit.getText().toString();
        mLogHandler.post(() -> {
            log.append("\nsendMessage --> Sending message: " + message);
        });

        byte[] messageBytes = Utilis.bytesFromString(message);
        if (messageBytes.length == 0) {
            mLogHandler.post(() -> {
                log.append("\nsendMessage --> Unable to convert message to bytes");
            });
            return;
        }

        characteristic.setValue(messageBytes);
        boolean success = mGatt.writeCharacteristic(characteristic);
        if (success) {
            mLogHandler.post(() -> {
                log.append("\nsendMessage --> Wrote: " + Utilis.byteArrayInHexFormat(messageBytes));
            });
        } else {
            mLogHandler.post(() -> {
                log.append("\nsendMessage --> Failed to write data");
            });
        }
    }

    private void sendImage(byte[] data, boolean isImage) {
        if (!mConnected) {
            mLogHandler.post(() -> {
                log.append("\nsendImage --> Not connected");
            });
            return;
        }

        BluetoothGattCharacteristic characteristic = Utilis.findEchoCharacteristic(mGatt);
        if (characteristic == null) {
            mLogHandler.post(() -> {
                log.append("\nsendImage --> Unable to find echo characteristic");
            });
            disconnectGattServer();
            return;
        }




        if(isImage) {
            String first = "image";
            String size = "" + data.length;

            byte[] first_convert = Utilis.bytesFromString(first);
            characteristic.setValue(first_convert);
            boolean success = mGatt.writeCharacteristic(characteristic);
            if (success) {
                mLogHandler.post(() -> {
                    log.append("\nsendMessage --> Wrote: " + Utilis.byteArrayInHexFormat(first_convert));
                });
            } else {
                mLogHandler.post(() -> {
                    log.append("\nsendMessage --> Failed to write data");
                });
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            byte[] second_converted = Utilis.bytesFromString(size);
            characteristic.setValue(second_converted);
            success = mGatt.writeCharacteristic(characteristic);
            if (success) {
                mLogHandler.post(() -> {
                    log.append("\nsendMessage --> Wrote: " + Utilis.byteArrayInHexFormat(second_converted));
                });
            } else {
                mLogHandler.post(() -> {
                    log.append("\nsendMessage --> Failed to write data");
                });
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int n = 0;
            while(n < data.length) {
                if(n + DEFAULT_BYTES_VIA_BLE >= data.length) {
                    int i = 0;
                    byte[] lastPacket = new byte[data.length - n];
                    for(int j = n; j < data.length; j++) {
                        lastPacket[i] = data[j];
                        i++;
                        n++;
                    }
                    characteristic.setValue(lastPacket);
                    success = mGatt.writeCharacteristic(characteristic);
                    if (success) {
                        mLogHandler.post(() -> {
                            log.append("\nsendImage --> Wrote: " + lastPacket);
                        });
                    } else {
                        mLogHandler.post(() -> {
                            log.append("\nsendImage --> Failed to write data");
                        });
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    byte[] first_middle = new byte[DEFAULT_BYTES_VIA_BLE];
                    for(int k = 0; k < DEFAULT_BYTES_VIA_BLE; k++) {
                        first_middle[k] = data[n];
                        n++;
                    }
                    characteristic.setValue(first_middle);
                    success = mGatt.writeCharacteristic(characteristic);
                    if (success) {
                        mLogHandler.post(() -> {
                            log.append("\nsendImage --> Wrote: " + first_middle);
                        });
                    } else {
                        mLogHandler.post(() -> {
                            log.append("\nsendImage --> Failed to write data");
                        });
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
            return;
        }
        else {


            String person = "dataPerson";
            String size = "" + data.length;


            byte[] first_convert = Utilis.bytesFromString(person);
            characteristic.setValue(first_convert);
            boolean success = mGatt.writeCharacteristic(characteristic);
            if (success) {
                mLogHandler.post(() -> {
                    log.append("\nsendData --> Wrote: " + first_convert);
                });
            } else {
                mLogHandler.post(() -> {
                    log.append("\nsendData --> Failed to write data");
                });
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            byte[] second_converted = Utilis.bytesFromString(size);
            characteristic.setValue(second_converted);
            success = mGatt.writeCharacteristic(characteristic);
            if (success) {
                mLogHandler.post(() -> {
                    log.append("\nsendData --> Wrote: " + Utilis.byteArrayInHexFormat(second_converted));
                });
            } else {
                mLogHandler.post(() -> {
                    log.append("\nsendData --> Failed to write data");
                });
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            int n = 0;
            while(n < data.length) {
                if(n + DEFAULT_BYTES_VIA_BLE >= data.length) {
                    int i = 0;
                    byte[] lastPacket = new byte[data.length - n];
                    for(int j = n; j < data.length; j++) {
                        lastPacket[i] = data[j];
                        i++;
                        n++;
                    }
                    characteristic.setValue(lastPacket);
                    success = mGatt.writeCharacteristic(characteristic);
                    if (success) {
                        mLogHandler.post(() -> {
                            log.append("\nsendData --> Wrote: " + lastPacket);
                        });
                    } else {
                        mLogHandler.post(() -> {
                            log.append("\nsendData --> Failed to write data");
                        });
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    byte[] first_middle = new byte[DEFAULT_BYTES_VIA_BLE];
                    for(int k = 0; k < DEFAULT_BYTES_VIA_BLE; k++) {
                        first_middle[k] = data[n];
                        n++;
                    }
                    characteristic.setValue(first_middle);
                    success = mGatt.writeCharacteristic(characteristic);
                    if (success) {
                        mLogHandler.post(() -> {
                            log.append("\nsendData --> Wrote: " + first_middle);
                        });
                    } else {
                        mLogHandler.post(() -> {
                            log.append("\nsendData --> Failed to write data");
                        });
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
            return;
        }

    }

/* ------------------------------------------------------------------------------ */

/* ----------------------------- UTILITY ----------------------------- */

    public void setConnected(boolean connected) {
        mConnected = connected;
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
