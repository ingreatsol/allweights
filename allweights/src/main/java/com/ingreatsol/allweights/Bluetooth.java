package com.ingreatsol.allweights;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;

import java.io.IOException;
import java.util.UUID;

public class Bluetooth extends AsyncTask<Void , Void, Void>
{
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private boolean ConnectSuccess = false;
    BluetoothAdapter myBluetooth = null;
    BluetoothDevice dispositivo = null;
    private final String address;
    private final Bluetooth_listener listener;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public Bluetooth(Bluetooth_listener _listener, String ADDRESS){
        this.listener = _listener;
        this.address = ADDRESS;
    }

    public boolean encerar(){
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write("0".toString().getBytes());
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    protected void onPreExecute() {
    }

    @SuppressLint("MissingPermission")
    @Override
    protected Void doInBackground(Void... params) {
        try {
            if (btSocket == null || !isBtConnected) {
                myBluetooth = BluetoothAdapter.getDefaultAdapter();
                dispositivo = myBluetooth.getRemoteDevice(this.address);
                btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                btSocket.connect();
                ConnectSuccess = true;
            }
        } catch (IOException e) {
            ConnectSuccess = false;
        }
        return null;
    }

    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        if (!ConnectSuccess) {
            super.onCancelled();
            try {
                btSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //finish();
            listener.onFinisched();
        } else {
            isBtConnected = true;
            listener.initask(btSocket, dispositivo);

        }
    }

    @Override
    protected void finalize() throws Throwable {
        btSocket.close();
        super.finalize();
    }

    public void finish(){
        try {
            finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}

