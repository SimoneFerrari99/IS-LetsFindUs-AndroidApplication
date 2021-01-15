package com.example.lets_findus.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;

import com.example.lets_findus.utilities.Person;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.example.lets_findus.bluetooth.GattAttributes.SERVICE_UUID;
import static com.example.lets_findus.bluetooth.Utils.findCharacteristics;

public class Client {

    private final Context context;
    private final Activity activity;
    private final Person myProfile;

    Handler mLogHandler = new Handler(Looper.getMainLooper());

    private Object dummy;

    private boolean mConnected;
    private final BluetoothAdapter mBluetoothAdapter;
    private boolean mEchoInitialized;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothGatt mGatt;
    private static final long SCAN_PERIOD = 5000;


    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_FINE_LOCATION = 2;

    private boolean mScanning;
    private Handler mHandler;
    private Map<String, BluetoothDevice> mScanResults;

    private static final int DEFAULT_BYTES_VIA_BLE = 512;

    public Client(Activity activity, Person myProfile, BluetoothAdapter mBluetoothAdapter) {
        this.context = activity.getApplicationContext();
        this.activity = activity;
        this.myProfile = myProfile;
        this.mBluetoothAdapter = mBluetoothAdapter;
        dummy = new Object();
    }


    public void startScan() {
        if (!hasPermissions() || mScanning) {
            return;
        }

        disconnectGattServer();
        //String, BluetoothDevice
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
        //stoppo la scansione dopo un tot di tempo ossia SCAN PERIOD
        mHandler.postDelayed(this::stopScan, SCAN_PERIOD);

        mScanning = true;
    }

    private void stopScan() {
        if (mScanning && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothLeScanner != null) {
            mBluetoothLeScanner.stopScan(scanCallback);
            scanComplete();
        }

        scanCallback = null;
        mScanning = false;
        mHandler = null;
    }

    //PER OGNI DEVICE LO PRENDE E SI CONNETTE
    private void scanComplete() {
        if (!mScanResults.isEmpty()) {
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
        return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        activity.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
    }

    private void requestBluetoothEnable() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    /* ------------------------------------------------------------------------------ */

    /* ----------------------------- CONNESSIONE AL DEVICE ----------------------------- */

    private void connectDevice(BluetoothDevice device) {
        mGatt = device.connectGatt(context, false, gattCallback);
        try {
            Thread.sleep(2000);
            //inizializzo tutti i dati da mandare
            Gson gson = new Gson();
            //File image = new File(myProfile.profilePath);
            byte[] imageToSend = Utils.imageToByte(myProfile.profilePath, context);
            //non serve mandare il path, quindi lo rimuovo per alleggerire i dati da mandare
            myProfile.profilePath = "";
            String personJson = gson.toJson(myProfile);
            byte[] profileToSend = Utils.bytesFromString(personJson);
            //prima mando la stringa
            sendData(profileToSend, false);
            //aspetto circa mezzo secondo
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    sendData(imageToSend, true);
                }
            }, 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /* ------------------------------------------------------------------------------ */

    /* ----------------------------- SCAN CALLBACK ----------------------------- */

    ScanCallback scanCallback = new ScanCallback() {

        //INSERISCO I DISPOSITIVI TROVATI NELLA LISTA
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
                BluetoothDevice device = result.getDevice();
                String deviceAddress = device.getAddress();
                mScanResults.put(deviceAddress, device);
            }
        }
    };

    /* ------------------------------------------------------------------------------ */

    /* ----------------------------- GATT CALLBACK ----------------------------- */


    BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        //vari step di connessione
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    setConnected(true);
                    gatt.requestMtu(517);
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    //sono stato respinto
                    break;
                default:
                    disconnectGattServer();
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            List<BluetoothGattCharacteristic> matchingCharacteristics = findCharacteristics(gatt);

            if (matchingCharacteristics.isEmpty()) {
                return;
            }
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }

            boolean characteristicWriteSuccess = false;

            for (BluetoothGattCharacteristic characteristic : matchingCharacteristics) {
                characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                characteristicWriteSuccess = gatt.setCharacteristicNotification(characteristic, true);
                if (characteristicWriteSuccess) {
                    if (Utils.isEchoCharacteristic(characteristic)) {
                        initializeEcho();
                    } else if (Utils.isTimeCharacteristic(characteristic)) {
                        List<BluetoothGattDescriptor> descriptorList = characteristic.getDescriptors();
                        BluetoothGattDescriptor descriptor = Utils.findClientConfigurationDescriptor(descriptorList);
                        if (descriptor == null) {
                            return;
                        }
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }
                }
            }
            if(!characteristicWriteSuccess) {
                disconnectGattServer();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status != BluetoothGatt.GATT_SUCCESS) {
                disconnectGattServer();
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            //una volta che ho la connessione devo capire che servizi posso usare
            gatt.discoverServices();
        }
    };

    /* ------------------------------------------------------------------------------ */

    /* ----------------------------- INVIO DEL DATO ----------------------------- */
    private Future<Boolean> sendPacket(byte[] packet, BluetoothGattCharacteristic characteristic){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        return executor.submit(()->{
            characteristic.setValue(packet);
            boolean success = mGatt.writeCharacteristic(characteristic);
            Thread.sleep(300);
            return success;
        });
    }

    public void sendData(byte[] data, boolean isImage){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(()->{
            try {
                BluetoothGattCharacteristic characteristic = Utils.findEchoCharacteristic(mGatt);
                if (characteristic == null) {
                    disconnectGattServer();
                    return;
                }

                boolean success;
                Future<Boolean> sended;
                String first;
                String size;

                if (isImage) {
                    first = "image";
                } else {
                    first = "dataPerson";
                }
                size = String.valueOf(data.length);

                byte[] first_convert = Utils.bytesFromString(first);
                sended = sendPacket(first_convert, characteristic);
                success = sended.get();
                if (!success) {
                    //SI PUO GESTIRE QUA IL NON INVIO DEL PACCHETTO
                    return;
                } else {
                }

                byte[] second_converted = Utils.bytesFromString(size);
                sended = sendPacket(second_converted, characteristic);
                success = sended.get();
                if (!success) {
                    //SI PUO GESTIRE QUA IL NON INVIO DEL PACCHETTO
                    return;
                } else {
                }

                int n = 0;
                while (n < data.length) {
                    if (n + DEFAULT_BYTES_VIA_BLE >= data.length) {
                        int i = 0;
                        byte[] lastPacket = new byte[data.length - n];
                        for (int j = n; j < data.length; j++) {
                            lastPacket[i] = data[j];
                            i++;
                            n++;
                        }
                        sended = sendPacket(lastPacket, characteristic);
                        success = sended.get();
                        if (!success) {
                            //SI PUO GESTIRE QUA IL NON INVIO DEL PACCHETTO
                            return;
                        }
                    } else {
                        byte[] first_middle = new byte[DEFAULT_BYTES_VIA_BLE];
                        for (int k = 0; k < DEFAULT_BYTES_VIA_BLE; k++) {
                            first_middle[k] = data[n];
                            n++;
                        }
                        sended = sendPacket(first_middle, characteristic);
                        success = sended.get();
                        if (!success) {
                            return;
                        }
                    }
                }
            }
            catch (ExecutionException | InterruptedException e){
                e.printStackTrace();
            }
        });
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
        boolean mTimeInitialized = false;
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
        }
    }

}
