package com.example.lets_findus;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.List;
import java.util.Map;

public class FirstScanCallback extends ScanCallback {

    private Map<String, BluetoothDevice> mScanResults;
    private Handler mLogHandler;

    FirstScanCallback(Map<String, BluetoothDevice> scanResults) {
        mScanResults = scanResults;
        mLogHandler = new Handler(Looper.getMainLooper());
    }

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
            Log.i("ScanResult", result.toString());
            BluetoothDevice device = result.getDevice();
            String deviceAddress = device.getAddress();
            mScanResults.put(deviceAddress, device);
        }
    }

    @Override
    public void onScanFailed(int errorCode) {
        Log.e("Scan Failed", "Error Code: " + errorCode);
    }

}
