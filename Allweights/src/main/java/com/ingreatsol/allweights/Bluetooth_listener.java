package com.ingreatsol.allweights;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

interface Bluetooth_listener {

    void onFinisched();

    void OnResult(String result);

    void initask(BluetoothSocket bluetoothSocket, BluetoothDevice bluetoothDevice);

}
