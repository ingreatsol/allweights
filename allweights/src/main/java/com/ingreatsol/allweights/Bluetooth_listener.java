package com.ingreatsol.allweights;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public interface Bluetooth_listener {

    public void onFinisched();

    public void OnResult(String result);

    public void initask(BluetoothSocket bluetoothSocket, BluetoothDevice bluetoothDevice);

}
