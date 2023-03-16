package com.ingreatsol.allweights;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ingreatsol.allweights.exceptions.AllweightsException;

import java.util.ArrayList;

public class AllweightsScan {
    public static final String TAG = AllweightsScan.class.getSimpleName();
    private final MutableLiveData<Boolean> mScanning = new MutableLiveData<>(false);
    private final MutableLiveData<ArrayList<BluetoothDevice>> devices = new MutableLiveData<>();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    public static final long SCAN_PERIOD = 10000;
    private final ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            ArrayList<BluetoothDevice> currentDevices = devices.getValue();

            if (currentDevices == null) {
                currentDevices = new ArrayList<>();
            }

            currentDevices.add(result.getDevice());

            devices.setValue(currentDevices);
        }
    };
    private ActivityResultLauncher<Intent> enable_ble_launcher;

    @SuppressLint("MissingPermission")
    public void init(@NonNull FragmentActivity activity) {
        enable_ble_launcher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            scan(activity);
                        } catch (AllweightsException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
    }

    @SuppressLint("MissingPermission")
    public void init(@NonNull Fragment fragment) {
        enable_ble_launcher = fragment.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            scan(fragment.requireActivity());
                        } catch (AllweightsException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                });
    }

    public LiveData<ArrayList<BluetoothDevice>> getDevices() {
        return devices;
    }

    public LiveData<Boolean> getScanState() {
        return mScanning;
    }

    @RequiresPermission("android.permission.BLUETOOTH_SCAN")
    public void stopScan() {
        if (Boolean.TRUE.equals(mScanning.getValue())) {
            mScanning.setValue(false);
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_SCAN")
    public void cancelDiscovery() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_SCAN")
    public void scan(@NonNull FragmentActivity activity) throws AllweightsException {
        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            throw new AllweightsException("No soporta tecnologia ble");
        }

        if (AllweightsUtils.isMissingPermisionsEscanearBluetooth(activity.getApplication())) {
            throw new AllweightsException("Faltan permisos");
        }

        BluetoothAdapter mBluetoothAdapter = getmBluetoothAdapter(activity);

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            throw new AllweightsException("No soporta conexión bluetooth.");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            LaunchEnableBle();
            return;
        }

        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        if (Boolean.FALSE.equals(mScanning.getValue())) {
            // Stops scanning after a predefined scan period.
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                mScanning.setValue(false);
                bluetoothLeScanner.stopScan(leScanCallback);
            }, SCAN_PERIOD);

            mScanning.setValue(true);
            devices.setValue(new ArrayList<>());
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            mScanning.setValue(false);
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    private void LaunchEnableBle() {
        Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enable_ble_launcher.launch(enableBT);
    }

    private BluetoothAdapter getmBluetoothAdapter(FragmentActivity activity) {
        if (mBluetoothAdapter == null) {
            final BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }
        return mBluetoothAdapter;
    }
}
