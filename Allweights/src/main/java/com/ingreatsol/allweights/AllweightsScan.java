package com.ingreatsol.allweights;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.ingreatsol.allweights.exceptions.AllweightsException;

import java.util.ArrayList;

public class AllweightsScan {
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

    public LiveData<ArrayList<BluetoothDevice>> getDevices() {
        return devices;
    }

    public LiveData<Boolean> getScanState() {
        return mScanning;
    }

    @SuppressLint("MissingPermission")
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

        if (!isSuportBluetoothConnection(activity)) {
            throw new AllweightsException("No soporta conexión bluetooth.");
        }

        if (!AllweightsUtils.isBluethoothEnabled(activity)) {
            throw new AllweightsException("Bluetooth no habilitado");
        }

        if (!AllweightsUtils.isLocationEnabled(activity)) {
            throw new AllweightsException("Ubicación no habilitada");
        }

        if (AllweightsUtils.isMissingPermisionLocation(activity)) {
            throw new AllweightsException("Faltan permisos de ubicacion");
        }

        if (AllweightsUtils.isMissingPermisionBluetooth(activity)) {
            throw new AllweightsException("Faltan permisos de bluetooth");
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

    public boolean isSuportBluetoothConnection(Context activity){
        return getmBluetoothAdapter(activity) != null;
    }

    private BluetoothAdapter getmBluetoothAdapter(Context activity) {
        if (mBluetoothAdapter == null) {
            final BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }
        return mBluetoothAdapter;
    }
}
