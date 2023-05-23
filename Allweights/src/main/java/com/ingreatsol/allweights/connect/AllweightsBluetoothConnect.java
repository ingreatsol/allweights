package com.ingreatsol.allweights.connect;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.ingreatsol.allweights.common.AllweightsException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class AllweightsBluetoothConnect extends AllweightsConnect {
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param context The UI Activity Context
     */
    public AllweightsBluetoothConnect(Context context) {
        super(context, PackageManager.FEATURE_BLUETOOTH);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     */
    @Override
    @RequiresPermission(allOf = {
            "android.permission.BLUETOOTH_CONNECT",
            "android.permission.BLUETOOTH_SCAN"
    })
    public synchronized void connect() throws AllweightsException {
        super.connect();
        Log.d(TAG, "connect to: " + mBluetoothDeviceAddress);

        // Cancel any thread attempting to make a connection
        if (getConnectionStatus() == ConnectionStatus.CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread();
        mConnectThread.start();
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     */
    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private synchronized void connected(BluetoothSocket socket, final String socketType) {
        Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();
    }

    /**
     * Stop all threads
     */
    @Override
    public synchronized void disconnect() {
        super.disconnect();
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        newConnectionStatus(ConnectionStatus.DISCONNECTED);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @see ConnectedThread#write(byte[])
     */
    @Override
    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    public boolean sendMessage(String message) {
        super.sendMessage(message);
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (getConnectionStatus() != ConnectionStatus.CONNECTED) return false;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        return r.write(message.getBytes());
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private String mSocketType;

        @RequiresPermission(allOf = {
                "android.permission.BLUETOOTH_CONNECT",
                "android.permission.BLUETOOTH_SCAN"
        })
        public ConnectThread() {
            BluetoothDevice mmDevice = mBluetoothAdapter.getRemoteDevice(mBluetoothDeviceAddress);
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            try {
                if (deviceType > 1) {
                    mSocketType = "Secure";
                    tmp = mmDevice.createRfcommSocketToServiceRecord(myUUID);
                } else {
                    mSocketType = "Insecure";
                    tmp = mmDevice.createInsecureRfcommSocketToServiceRecord(myUUID);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
            newConnectionStatus(ConnectionStatus.CONNECTING);
        }


        @Override
        @RequiresPermission(allOf = {
                "android.permission.BLUETOOTH_CONNECT",
                "android.permission.BLUETOOTH_SCAN"
        })
        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread " + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (AllweightsBluetoothConnect.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
        public ConnectedThread(@NonNull BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            newConnectionStatus(ConnectionStatus.CONNECTED);
            activateWeight();
        }

        @Override
        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (this.mmSocket.isConnected()) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    String strReceived = new String(buffer, 0, bytes);
                    procesardatos(strReceived);
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    break;
                }
            }
            newConnectionStatus(ConnectionStatus.DISCONNECTED);
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public boolean write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
                return false;
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
