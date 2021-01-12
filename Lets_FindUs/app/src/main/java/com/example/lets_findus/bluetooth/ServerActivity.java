package com.example.lets_findus.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.example.lets_findus.bluetooth.GattAttributes.CHARACTERISTIC_ECHO_UUID;
import static com.example.lets_findus.bluetooth.GattAttributes.CHARACTERISTIC_TIME_UUID;
import static com.example.lets_findus.bluetooth.GattAttributes.CLIENT_CONFIGURATION_DESCRIPTOR_UUID;
import static com.example.lets_findus.bluetooth.GattAttributes.SERVICE_UUID;

public class ServerActivity extends AppCompatActivity {

    private Handler mLogHandler;
    private List<BluetoothDevice> mDevices;
    private Map<String, byte[]> mClientConfigurations;

    private BluetoothGattServer mGattServer;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;


    private boolean isImage = false;
    private boolean array_initialized = false;
    private boolean data = false;
    private int n = 0;
    private byte[] packet;
    private String size;

    /* ----------------------------- LIFECYCLE ----------------------------- */

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLogHandler = new Handler(Looper.getMainLooper());
        mDevices = new ArrayList<>();
        mClientConfigurations = new HashMap<>();

        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        //starting server
        setupServer();
        startAdvertising();

        //stopping server
        stopAdvertising();
        stopServer();

        //restart server
        restartServer();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if bluetooth is enabled
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            // Request user to enable it
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }

        // Check low energy support
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No BLE Support", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Check advertising
        if (!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
            // Unable to run the server on this device, get a better device
            Toast.makeText(this, "No Advertising Support", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

        mGattServer = mBluetoothManager.openGattServer(this, bluetoothGattServerCallback);

        setupServer();
        startAdvertising();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //probabilmente da togliere
        stopAdvertising();
        stopServer();
    }

    /* ------------------------------------------------------------------------------ */

    /* ----------------------------- SETUP SERVER ----------------------------- */

    private void setupServer() {
        BluetoothGattService service = new BluetoothGattService(SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        // Write characteristic
        BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(
                CHARACTERISTIC_ECHO_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);

        // Characteristic with Descriptor
        BluetoothGattCharacteristic notifyCharacteristic = new BluetoothGattCharacteristic(
                CHARACTERISTIC_TIME_UUID,
                0,
                0);

        BluetoothGattDescriptor clientConfigurationDescriptor = new BluetoothGattDescriptor(
                CLIENT_CONFIGURATION_DESCRIPTOR_UUID,
                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);
        clientConfigurationDescriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);

        notifyCharacteristic.addDescriptor(clientConfigurationDescriptor);

        service.addCharacteristic(writeCharacteristic);
        service.addCharacteristic(notifyCharacteristic);

        mGattServer.addService(service);
    }

    private void stopServer() {
        if (mGattServer != null) {
            mGattServer.close();
        }
    }

    private void restartServer() {
        stopAdvertising();
        stopServer();
        setupServer();
        startAdvertising();
    }

    /* ------------------------------------------------------------------------------ */

    /* ----------------------------- SETUP SERVICE ----------------------------- */

    private void startAdvertising() {
        if (mBluetoothLeAdvertiser == null) {
            return;
        }

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .build();

        ParcelUuid parcelUuid = new ParcelUuid(SERVICE_UUID);
        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .addServiceUuid(parcelUuid)
                .build();

        mBluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback);
    }

    private void stopAdvertising() {
        if (mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        }
    }

    private final AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            mLogHandler.post(() -> {
                //la tv log non esiste, bisognerà capire cosa fare gg
                //log.append("\nPeripheral advertising started");
            });

        }

        @Override
        public void onStartFailure(int errorCode) {
            //la tv log non esiste, bisognerà capire cosa fare gg
            mLogHandler.post(() -> {
                if (errorCode == 3) {
                    //log.append("\nPeripheral advertising already initialized");
                } else {
                    //log.append("\nPeripheral advertising failed: " + errorCode);
                }
            });
        }
    };

    /* ------------------------------------------------------------------------------ */

    /* ----------------------------- NOTIFICATIONS ----------------------------- */


    private void notifyCharacteristic(byte[] value, UUID uuid) {
        BluetoothGattService service = mGattServer.getService(SERVICE_UUID);
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(uuid);
        characteristic.setValue(value);
        // Indications require confirmation, notifications do not
        boolean confirm = Utils.requiresConfirmation(characteristic);
        for (BluetoothDevice device : mDevices) {
            if (clientEnabledNotifications(device, characteristic)) {
                mGattServer.notifyCharacteristicChanged(device, characteristic, confirm);
            }
        }
    }

    private boolean clientEnabledNotifications(BluetoothDevice device, BluetoothGattCharacteristic characteristic) {
        List<BluetoothGattDescriptor> descriptorList = characteristic.getDescriptors();
        BluetoothGattDescriptor descriptor = Utils.findClientConfigurationDescriptor(descriptorList);
        if (descriptor == null) {
            // There is no client configuration descriptor, treat as true
            return true;
        }
        String deviceAddress = device.getAddress();
        byte[] clientConfiguration = mClientConfigurations.get(deviceAddress);
        if (clientConfiguration == null) {
            // Descriptor has not been set
            return false;
        }

        byte[] notificationEnabled = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
        return clientConfiguration.length == notificationEnabled.length
                && (clientConfiguration[0] & notificationEnabled[0]) == notificationEnabled[0]
                && (clientConfiguration[1] & notificationEnabled[1]) == notificationEnabled[1];
    }

    /* ------------------------------------------------------------------------------ */

    /* ----------------------------- SERVER CALLBACK ----------------------------- */

    BluetoothGattServerCallback bluetoothGattServerCallback = new BluetoothGattServerCallback() {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);
            //la tv log non esiste, bisognerà capire cosa fare gg
            mLogHandler.post(() -> {
                /*log.append("\nonConnectionStateChange " + device.getAddress()
                        + "\nstatus " + status
                        + "\nnewState " + newState);*/
            });

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    mLogHandler.post(() -> {
                        //log.append("\nDevice added: " + device.getAddress());
                    });
                    mDevices.add(device);
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    mLogHandler.post(() -> {
                        //log.append("\nDevice removed : " + device.getAddress());
                    });
                    mDevices.remove(device);
                    String deviceAddress = device.getAddress();
                    mClientConfigurations.remove(deviceAddress);
                    break;
                default:
                    mLogHandler.post(() -> {
                        //log.append("\nError device: " + device.getAddress() + ", status: " + status + ", newState: " + newState);
                    });
            }

        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            mLogHandler.post(() -> {
                //la tv log non esiste, bisognerà capire cosa fare gg
                //log.append("\nonCharacteristicReadRequest " + characteristic.getUuid().toString());
            });

            if (Utils.requiresResponse(characteristic)) {
                // Unknown read characteristic requiring response, send failure
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null);
            }
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
            mLogHandler.post(() -> {
                //la tv log non esiste, bisognerà capire cosa fare gg
                /*log.append("\nonCharacteristicWriteRequest" + characteristic.getUuid().toString()
                        + "\nReceived: " + Utils.byteArrayInHexFormat(value));*/
            });

            try {
                mergePacket(value);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            if (CHARACTERISTIC_ECHO_UUID.equals(characteristic.getUuid())) {
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);

                characteristic.setValue(value);
                mLogHandler.post(() -> {
                    //la tv log non esiste, bisognerà capire cosa fare gg
                    //log.append("\nSending: " + Utils.byteArrayInHexFormat(value));
                });
                notifyCharacteristic(value, CHARACTERISTIC_ECHO_UUID);
            }
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
            super.onDescriptorReadRequest(device, requestId, offset, descriptor);
            mLogHandler.post(() -> {
                //la tv log non esiste, bisognerà capire cosa fare gg
                //log.append("\nonDescriptorReadRequest" + descriptor.getUuid().toString());
            });
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
            mLogHandler.post(() -> {
                /*log.append("\nonDescriptorWriteRequest: " + descriptor.getUuid().toString()
                        + "\nvalue: " + Utils.byteArrayInHexFormat(value));*/
            });

            if (CLIENT_CONFIGURATION_DESCRIPTOR_UUID.equals(descriptor.getUuid())) {
                String deviceAddress = device.getAddress();
                mClientConfigurations.put(deviceAddress, value);
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
            }
        }

        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            mLogHandler.post(() -> {
                //la tv log non esiste, bisognerà capire cosa fare gg
                //log.append("\nonNotificationSent");
            });

        }

        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            super.onMtuChanged(device, mtu);
            mLogHandler.post(() -> {
                //la tv log non esiste, bisognerà capire cosa fare gg
                //log.append("\nonMtuChanged --> Mtu changed: " + mtu);
            });
        }
    };

    /* ------------------------------------------------------------------------------ */


    public void mergePacket(byte[] value) throws InterruptedException {
        if(!isImage && !data) {
            if(Utils.byteToString(value).equals("image")) {
                isImage = true;
            }
            else if(Utils.byteToString(value).equals("dataPerson")) {
                data = true;
            }
            else{
                //ghemo da fare qualcosa
            }
            return;
        }
        else {
            if(!array_initialized) {
                size = Utils.byteToString(value);
                packet = setSizeByteArray(Integer.parseInt(size));
                array_initialized = true;
                return;
            }
            else {
                for (byte b : value) {
                    packet[n] = b;
                    n++;
                }
            }
        }
        if(n >= Integer.parseInt(size)) {
            isImage = false;
            array_initialized = false;
            data = false;
            n = 0;
            Log.i("mergePacket", "------------" + Utils.byteToString(packet) + "------------");
            mLogHandler.post(() -> {
                //la tv log non esiste, bisognerà capire cosa fare gg
                //log.append("\nmergePacket --> ------------ " + packet.toString() + "------------");
            });
        }
    }

    public byte[] setSizeByteArray(int size) {
        return new byte[size];
    }

}
