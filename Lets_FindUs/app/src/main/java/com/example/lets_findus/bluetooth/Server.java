package com.example.lets_findus.bluetooth;

import android.Manifest;
import android.app.Activity;
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
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Log;

import com.example.lets_findus.utilities.Meeting;
import com.example.lets_findus.utilities.MeetingDao;
import com.example.lets_findus.utilities.Person;
import com.example.lets_findus.utilities.PersonDao;
import com.example.lets_findus.utilities.UtilFunction;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static com.example.lets_findus.bluetooth.GattAttributes.CHARACTERISTIC_ECHO_UUID;
import static com.example.lets_findus.bluetooth.GattAttributes.CHARACTERISTIC_TIME_UUID;
import static com.example.lets_findus.bluetooth.GattAttributes.CLIENT_CONFIGURATION_DESCRIPTOR_UUID;
import static com.example.lets_findus.bluetooth.GattAttributes.SERVICE_UUID;

public class Server {

    private Context context;
    private Activity activity;

    private Person lastSavedPerson;
    private boolean personSaved = false;

    private final MeetingDao md;
    private final PersonDao pd;

    private final FusedLocationProviderClient fusedLocationClient;

    private final List<BluetoothDevice> mDevices;
    private final Map<String, byte[]> mClientConfigurations;

    private final BluetoothGattServer mGattServer;
    private final BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    Handler mLogHandler = new Handler(Looper.getMainLooper());

    private boolean isImage = false;
    private boolean array_initialized = false;
    private boolean data = false;
    private int n = 0;
    private byte[] packet;
    private String size;

    public Server(Activity activity, MeetingDao md, PersonDao pd, BluetoothManager mBluetoothManager) {
        this.context = activity.getApplicationContext();
        this.activity = activity;
        this.md = md;
        this.pd = pd;
        BluetoothAdapter mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        mDevices = new ArrayList<>();
        mClientConfigurations = new HashMap<>();
        mGattServer = mBluetoothManager.openGattServer(context, bluetoothGattServerCallback);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    public void setupServer() {
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

    public void stopServer() {
        if (mGattServer != null) {
            mGattServer.close();
        }
    }

    public void restartServer() {
        stopAdvertising();
        stopServer();
        setupServer();
        startAdvertising();
    }

    /* ------------------------------------------------------------------------------ */

    /* ----------------------------- SETUP SERVICE ----------------------------- */

    public void startAdvertising() {
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

    public void stopAdvertising() {
        if (mBluetoothLeAdvertiser != null) {
            mBluetoothLeAdvertiser.stopAdvertising(mAdvertiseCallback);
        }
    }

    //questa cosa è praticamente inutile
    private final AdvertiseCallback mAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            /*mLogHandler.post(() -> {
                //la tv log non esiste, bisognerà capire cosa fare gg
                //log.append("\nPeripheral advertising started");
            });*/

        }

        @Override
        public void onStartFailure(int errorCode) {
            //la tv log non esiste, bisognerà capire cosa fare gg
            /*mLogHandler.post(() -> {
                if (errorCode == 3) {
                    //log.append("\nPeripheral advertising already initialized");
                } else {
                    //log.append("\nPeripheral advertising failed: " + errorCode);
                }
            });*/
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

    //serve perchè potrebbero esserci dei problemi, boh vediamo se serve
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
    //va modificata questa per salvare le cose
    private BluetoothGattServerCallback bluetoothGattServerCallback = new BluetoothGattServerCallback() {

        //ogni dispositivo che che si connette viene buttato in una lista, se si disconnette o ha problemi viene rimosso
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            super.onConnectionStateChange(device, status, newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mDevices.add(device);
                mLogHandler.post(()->{
                    Log.d("Server", "onConnectionStateChange connesso" + status);
                });
            } else {
                mDevices.remove(device);
                String deviceAddress = device.getAddress();
                mClientConfigurations.remove(deviceAddress);
            }

        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
            mLogHandler.post(()->{
                Log.d("Server", "onCharacteristicReadRequest");
            });
            //se richiede una caratteristica che non ho lo dico al client
            if (Utils.requiresResponse(characteristic)) {
                // Unknown read characteristic requiring response, send failure
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null);
            }
        }

        //qua avviene la roba importante
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            mLogHandler.post(()->{
                Log.d("Server", "onCharacteristicWriteRequest");
            });
            try {
                mergePacket(value);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //conferma l'andato al buon fine della ricezione del pacchetto
            if (CHARACTERISTIC_ECHO_UUID.equals(characteristic.getUuid())) {
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
            }
            notifyCharacteristic(value, CHARACTERISTIC_ECHO_UUID);
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
            mLogHandler.post(()->{
                Log.d("Server", "onDescriptorWriteRequest fuori if");
            });
            if (CLIENT_CONFIGURATION_DESCRIPTOR_UUID.equals(descriptor.getUuid())) {
                mLogHandler.post(()->{
                    Log.d("Server", "onDescriptorWriteRequest dentro if");
                });
                String deviceAddress = device.getAddress();
                mClientConfigurations.put(deviceAddress, value);
                mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
            }
        }
    };

    /* ------------------------------------------------------------------------------ */


    public void mergePacket(byte[] value) throws InterruptedException {
        mLogHandler.post(()->{
            Log.d("Server", "sto ricevendo ghesbo");
        });
        if (!isImage && !data) {
            if (Utils.byteToString(value).equals("image")) {
                isImage = true;
            } else if (Utils.byteToString(value).equals("dataPerson")) {
                data = true;
            } else {
                //ghemo da fare qualcosa
            }
            return;
        } else {
            if (!array_initialized) {
                size = Utils.byteToString(value);
                packet = setSizeByteArray(Integer.parseInt(size));
                array_initialized = true;
                return;
            } else {
                for (byte b : value) {
                    packet[n] = b;
                    n++;
                }
            }
        }
        if (n >= Integer.parseInt(size)) {
            //qua è quando finisce
            //l'array packet contiene i miei dati, dovrò salvarli subito perchè poi vengono distrutti
            if (!isImage) {
                lastSavedPerson = Person.getPersonFromString(Utils.stringFromBytes(packet));
                personSaved = true;
                mLogHandler.post(()->{
                    Log.d("Server", "go finio la persona ghesbo");
                });
            }
            if (isImage && personSaved) {
                try {
                    mLogHandler.post(()->{
                        Log.d("Server", "go finio l'immagine ghesbo");
                    });
                    File imageFile = UtilFunction.createImageFile(context);
                    lastSavedPerson.profilePath = Utils.saveImageFromByte(packet, imageFile);
                    ListenableFuture<Long> saved = pd.insert(lastSavedPerson);
                    saved.get();
                    ListenableFuture<Person> savedPerson = pd.getLastPersonInserted();
                    Person justSaved = savedPerson.get();
                    if (activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && activity.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){// && activity.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    Location currentLocation = fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, null).getResult();
                    Meeting m = new Meeting(justSaved.id, currentLocation.getLatitude(), currentLocation.getLongitude(), Calendar.getInstance().getTime());
                    md.insert(m);
                } catch (IOException | ExecutionException e) {
                    e.printStackTrace();
                }
                personSaved = false;
            }
            isImage = false;
            array_initialized = false;
            data = false;
            n = 0;
        }
    }

    private byte[] setSizeByteArray(int size) {
        return new byte[size];
    }
}
