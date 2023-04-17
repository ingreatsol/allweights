package com.ingreatsol.allweights;

import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

class TransmisionBluetooth extends AsyncTask<String, String, Void> {
    public static final String TAG = TransmisionBluetooth.class.getSimpleName();
    byte[] buffer = new byte[1024];
    int bytes;
    private final BluetoothSocket btSoket;
    private final BluetoothListener bluetooth_listener;

    public TransmisionBluetooth(BluetoothListener listener, BluetoothSocket btsoket) {
        bluetooth_listener = listener;
        this.btSoket = btsoket;
    }

    @Override
    protected Void doInBackground(String... strings) {
        while (this.btSoket.isConnected()) {
            try {
                Thread.sleep(10);
                bytes = this.btSoket.getInputStream().read(buffer);
                String strReceived = new String(buffer, 0, bytes);
                if (strReceived.length() > 30) {
                    strReceived = "";
                }
                publishProgress(strReceived);

            } catch (InterruptedException | IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return null;
    }

    protected void onProgressUpdate(@NonNull String... values) {
        bluetooth_listener.OnResult(values[0]);
    }

    @Override
    protected void onPostExecute(Void result) {

    }

    protected void onCancelled() {
        super.onCancelled();
        bluetooth_listener.onStatusConnection(ConnectionStatus.DISCONNECTED);
    }
}
