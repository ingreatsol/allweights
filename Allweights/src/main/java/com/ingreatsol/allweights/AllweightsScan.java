package com.ingreatsol.allweights;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.ingreatsol.allweights.exceptions.AllweightsException;

import java.util.ArrayList;

public class AllweightsScan {
    private Boolean mScanning = false;
    private final ArrayList<OnAllweightsScanStatusListener> mOnAllweightsScanStatusListener;
    private final ArrayList<OnBluetoothDeviceListener> mOnBluetoothDeviceListener;
    private BluetoothAdapter mBluetoothAdapter;
    public static final long SCAN_PERIOD = 10000;

    private final BroadcastReceiver mBluetoothDeviceUpdateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //may need to chain this to a recognizing function
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                for (OnBluetoothDeviceListener listener :
                        mOnBluetoothDeviceListener) {
                    listener.onBluetoothDevice(device);
                }
            }
        }
    };

    public AllweightsScan() {
        mOnAllweightsScanStatusListener = new ArrayList<>();
        mOnBluetoothDeviceListener = new ArrayList<>();
    }

    private void newScanStatus(Boolean status) {
        mScanning = status;
        for (OnAllweightsScanStatusListener listener : mOnAllweightsScanStatusListener) {
            listener.onAllweightsScanStatus(mScanning);
        }
    }

    public Boolean getScanStatus() {
        return mScanning;
    }

    public void addOnAllweightsScanStatusListener(OnAllweightsScanStatusListener listener) {
        mOnAllweightsScanStatusListener.add(listener);
    }

    public void removeOnAllweightsScanStatusListener(OnAllweightsScanStatusListener listener) {
        mOnAllweightsScanStatusListener.remove(listener);
    }

    public void clearOnConnectionStatusListener() {
        mOnAllweightsScanStatusListener.clear();
    }

    public void addOnBluetoothDeviceListener(OnBluetoothDeviceListener listener) {
        mOnBluetoothDeviceListener.add(listener);
    }

    public void removeOnBluetoothDeviceListener(OnBluetoothDeviceListener listener) {
        mOnBluetoothDeviceListener.remove(listener);
    }

    public void clearOnBluetoothDeviceListener() {
        mOnBluetoothDeviceListener.clear();
    }

    public void registerService(@NonNull Context context) {
        context.registerReceiver(mBluetoothDeviceUpdateReceiver, GattAttributes.makeBluetoothUpdateIntentFilter());
    }

    public void unRegisterService(@NonNull Context context) {
        context.unregisterReceiver(mBluetoothDeviceUpdateReceiver);
    }

    @SuppressLint("MissingPermission")
    public void stopScan() {
        if (mScanning) {
            getmBluetoothAdapter().cancelDiscovery();
            newScanStatus(false);
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_SCAN")
    public void scan(@NonNull Context activity) throws AllweightsException {
        if (!activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            throw new AllweightsException("No soporta tecnologia ble");
        }

        if (!isSuportBluetoothConnection()) {
            throw new AllweightsException("Este teléfono no tiene Bluetooth.");
        }

        if (!AllweightsUtils.isBluethoothEnabled()) {
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

        stopScan();

        new Handler(Looper.getMainLooper()).postDelayed(this::stopScan, SCAN_PERIOD);

        mBluetoothAdapter.startDiscovery();
        newScanStatus(true);
    }

    public boolean isSuportBluetoothConnection() {
        return getmBluetoothAdapter() != null;
    }

    private BluetoothAdapter getmBluetoothAdapter() {
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        return mBluetoothAdapter;
    }

    public interface OnAllweightsScanStatusListener {
        void onAllweightsScanStatus(Boolean status);
    }

    public interface OnBluetoothDeviceListener {
        void onBluetoothDevice(BluetoothDevice device);
    }
}
