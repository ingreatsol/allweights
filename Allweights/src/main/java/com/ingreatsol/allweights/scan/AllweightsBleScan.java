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
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device1 = result.getDevice();
                newDevice(device1);
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
        if (getScanStatus()) {
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
        BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();

        scanner.startScan(scanFilters, new ScanSettings.Builder().build(), discoveryCallback);

        newScanStatus(true);
    }
}
