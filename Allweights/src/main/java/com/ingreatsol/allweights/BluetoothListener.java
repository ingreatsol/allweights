package com.ingreatsol.allweights;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

interface BluetoothListener {
    void onStatusConnection(ConnectionStatus status);
    void OnResult(String result);
    void initask(BluetoothSocket bluetoothSocket, BluetoothDevice bluetoothDevice);
}
