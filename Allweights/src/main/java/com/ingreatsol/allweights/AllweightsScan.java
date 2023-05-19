package com.ingreatsol.allweights;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.ingreatsol.allweights.exceptions.AllweightsException;

import java.util.ArrayList;
import java.util.List;

public class AllweightsScan {
    private Boolean mScanning = false;
    private final ArrayList<OnAllweightsScanCallback> mOnAllweightsScanCallback;
    public static final long SCAN_PERIOD = 10000;
    private final Context context;
    private final BluetoothAdapter mBluetoothAdapter;
    private final ScanCallback discoveryCallback;
    private final Handler mMainHandler;
    protected final Object channelsLock = new Object();

    public AllweightsScan(@NonNull final Context context) {
        mOnAllweightsScanCallback = new ArrayList<>();
        this.context = context;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mMainHandler = new Handler(Looper.getMainLooper());
        discoveryCallback = new ScanCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public synchronized void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                synchronized (channelsLock) {
                    switch (callbackType) {
                        case ScanSettings.CALLBACK_TYPE_FIRST_MATCH: {
                            BluetoothDevice device1 = result.getDevice();
                            mMainHandler.post(() -> {
                                for (OnAllweightsScanCallback listener : mOnAllweightsScanCallback) {
                                    listener.onFoundBluetoothDevice(device1);
                                }
                            });
                            break;
                        }
                        case ScanSettings.CALLBACK_TYPE_MATCH_LOST: {
                            BluetoothDevice device1 = result.getDevice();
                            mMainHandler.post(() -> {
                                for (OnAllweightsScanCallback listener : mOnAllweightsScanCallback) {
                                    listener.onLossBluetoothDevice(device1);
                                }
                            });
                            break;
                        }
                    }
                }
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };
    }

    private void newScanStatus(Boolean status) {
        mScanning = status;
        for (OnAllweightsScanCallback listener : mOnAllweightsScanCallback) {
            listener.onAllweightsScanStatusChange(mScanning);
        }
    }

    public Boolean getScanStatus() {
        return mScanning;
    }

    public void addOnAllweightsScanCallback(OnAllweightsScanCallback listener) {
        mOnAllweightsScanCallback.add(listener);
    }

    public void removeOnAllweightsScanCallback(OnAllweightsScanCallback listener) {
        mOnAllweightsScanCallback.remove(listener);
    }

    public void clearOnAllweightsScanCallback() {
        mOnAllweightsScanCallback.clear();
    }

    @SuppressLint("MissingPermission")
    public void stopScan() {
        if (mScanning) {
            BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
            scanner.stopScan(discoveryCallback);
            newScanStatus(false);
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_SCAN")
    public void scan() throws AllweightsException {
        if (AllweightsUtils.isNotExistBluetoothInSystem(context)) {
            throw new AllweightsException("No soporta tecnologia ble");
        }

        if (!isSuportBluetoothConnection()) {
            throw new AllweightsException("Este teléfono no tiene Bluetooth.");
        }

        if (!AllweightsUtils.isBluethoothEnabled()) {
            throw new AllweightsException("Bluetooth no habilitado");
        }

        if (!AllweightsUtils.isLocationEnabled(context)) {
            throw new AllweightsException("Ubicación no habilitada");
        }

        if (AllweightsUtils.isMissingPermisionLocation(context)) {
            throw new AllweightsException("Faltan permisos de ubicacion");
        }

        if (AllweightsUtils.isMissingPermisionBluetooth(context)) {
            throw new AllweightsException("Faltan permisos de bluetooth");
        }

        stopScan();

        mMainHandler.postDelayed(this::stopScan, SCAN_PERIOD);

        ArrayList<ScanFilter> scanFilters = new ArrayList<>();
        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
                .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                .setReportDelay(0)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .build();
        BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();

        scanner.startScan(discoveryCallback);

        newScanStatus(true);
    }


    public boolean isSuportBluetoothConnection() {
        return mBluetoothAdapter != null;
    }

    public interface OnAllweightsScanCallback {
        void onFoundBluetoothDevice(BluetoothDevice device);

        void onLossBluetoothDevice(BluetoothDevice device);

        void onAllweightsScanStatusChange(Boolean status);
    }
}
