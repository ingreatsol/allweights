package com.ingreatsol.allweights.scan;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.ingreatsol.allweights.common.GattAttributes;
import com.ingreatsol.allweights.common.AllweightsException;

public class AllweightsBluetoothScan extends AllweightsScan {
    private final BroadcastReceiver mBluetoothDeviceUpdateReceiver;

    public AllweightsBluetoothScan(@NonNull final Context context) {
        super(context, PackageManager.FEATURE_BLUETOOTH);
        mBluetoothDeviceUpdateReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, @NonNull Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    newDevice(device);
                }
            }
        };
        registerService();
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerService() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(mBluetoothDeviceUpdateReceiver, GattAttributes.makeBluetoothUpdateIntentFilter(), Context.RECEIVER_EXPORTED);
            } else {
                context.registerReceiver(mBluetoothDeviceUpdateReceiver, GattAttributes.makeBluetoothUpdateIntentFilter());
            }
        } catch (Exception exception) {
            Log.d(TAG, "Register broacast reciever error", exception);
        }
    }

    private void unRegisterService() {
        try {
            context.unregisterReceiver(mBluetoothDeviceUpdateReceiver);
        } catch (Exception exception) {
            Log.d(TAG, "Unregister broacast reciever error", exception);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void stopScan() {
        super.stopScan();
        if (getScanStatus()) {
            mBluetoothAdapter.cancelDiscovery();
            newScanStatus(false);
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_SCAN")
    @Override
    public void scan() throws AllweightsException {
        super.scan();

        mBluetoothAdapter.startDiscovery();
        newScanStatus(true);
    }

    @Override
    public void destroy() {
        super.destroy();
        unRegisterService();
    }
}
