package com.ingreatsol.allweights.scan;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.ParcelUuid;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.ingreatsol.allweights.common.AllweightsException;
import com.ingreatsol.allweights.common.GattAttributes;

import java.util.ArrayList;
import java.util.List;

public class AllweightsBleScan extends AllweightsScan {
    private final ScanCallback discoveryCallback;

    public AllweightsBleScan(@NonNull Context context) {
        super(context, PackageManager.FEATURE_BLUETOOTH_LE);
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
                                for (AllweightsScanCallback listener : mOnAllweightsScanCallback) {
                                    listener.onFoundBluetoothDevice(device1);
                                }
                            });
                            break;
                        }
                        case ScanSettings.CALLBACK_TYPE_MATCH_LOST: {
                            BluetoothDevice device1 = result.getDevice();
                            mMainHandler.post(() -> {
                                for (AllweightsScanCallback listener : mOnAllweightsScanCallback) {
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

    @SuppressLint("MissingPermission")
    @Override
    public void stopScan() {
        if (mScanning) {
            BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();
            scanner.stopScan(discoveryCallback);
            newScanStatus(false);
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_SCAN")
    @Override
    public void scan() throws AllweightsException {
        super.scan();

        ArrayList<ScanFilter> scanFilters = new ArrayList<>();
        scanFilters.add(new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(GattAttributes.SERVICE_UUID))
                .build());
        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
                .setReportDelay(0)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_FIRST_MATCH | ScanSettings.CALLBACK_TYPE_MATCH_LOST)
                .build();
        BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();

        scanner.startScan(scanFilters, scanSettings, discoveryCallback);

        newScanStatus(true);
    }
}
