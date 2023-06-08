package com.ingreatsol.allweights.scan;

import android.bluetooth.BluetoothDevice;

public interface AllweightsScanCallback {
    void onFoundBluetoothDevice(BluetoothDevice device);

    void onAllweightsScanStatusChange(Boolean status);
}
