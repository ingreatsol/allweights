package com.ingreatsol.allweights;

import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import java.io.IOException;

class Transmision_bluetooth extends AsyncTask<String, String, Void>  // UI thread
{
    byte[] buffer = new byte[1024];
    int bytes;
    private final BluetoothSocket btSoket;
    private final Bluetooth_listener bluetooth_listener;

    public Transmision_bluetooth(Bluetooth_listener listener, BluetoothSocket btsoket){
        bluetooth_listener = listener;
        this.btSoket = btsoket;
    }

    @Override
    protected Void doInBackground(String... strings) {
        while(this.btSoket.isConnected()) {
            try {
                Thread.sleep(10);
                bytes = this.btSoket.getInputStream().read(buffer);
                String strReceived = new String(buffer, 0, bytes);
                if(strReceived.length() > 30){
                    strReceived = "";
                }
                publishProgress(strReceived);

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
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
        bluetooth_listener.onFinisched();
    }
}
