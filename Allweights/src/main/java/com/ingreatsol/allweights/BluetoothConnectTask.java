package com.ingreatsol.allweights;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import java.io.IOException;
import java.util.UUID;

class BluetoothConnectTask extends AsyncTask<Void, Void, Void> {
    public static final String TAG = BluetoothConnectTask.class.getSimpleName();
    BluetoothSocket btSocket = null;
    private ConnectionStatus connectionStatus = ConnectionStatus.DISCONNECTED;
    BluetoothAdapter myBluetooth = null;
    BluetoothDevice dispositivo = null;
    private final String address;
    private final BluetoothListener listener;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @RequiresPermission(allOf = {
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT"
    })
    public BluetoothConnectTask(BluetoothListener _listener, String ADDRESS) {
        this.listener = _listener;
        this.address = ADDRESS;
    }

    public boolean sendData(String data) {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(data.getBytes());
                return true;
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return false;
            }
        }
        return false;
    }

    @RequiresPermission(allOf = {
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT"
    })
    @Override
    protected Void doInBackground(Void... params) {
        try {
            if (btSocket == null || connectionStatus == ConnectionStatus.DISCONNECTED) {
                connectionStatus = ConnectionStatus.CONNECTING;
                listener.onStatusConnection(connectionStatus);

                myBluetooth = BluetoothAdapter.getDefaultAdapter();
                dispositivo = myBluetooth.getRemoteDevice(this.address);
                btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                myBluetooth.cancelDiscovery();
                btSocket.connect();
                connectionStatus = ConnectionStatus.CONNECTED;
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            connectionStatus = ConnectionStatus.DISCONNECTED;
        }
        return null;
    }

    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        if (connectionStatus == ConnectionStatus.CONNECTED) {
            listener.initask(btSocket, dispositivo);
        } else {
            super.onCancelled();
            try {
                btSocket.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }

        listener.onStatusConnection(connectionStatus);
    }

    @Override
    protected void finalize() throws Throwable {
        btSocket.close();
        connectionStatus = ConnectionStatus.DISCONNECTED;
        listener.onStatusConnection(connectionStatus);
        super.finalize();
    }

    public void finish() {
        try {
            finalize();
        } catch (Throwable throwable) {
            Log.e(TAG, throwable.getMessage());
        }
    }
}

