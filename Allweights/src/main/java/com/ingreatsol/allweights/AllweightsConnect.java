package com.ingreatsol.allweights;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothStatusCodes;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.ingreatsol.allweights.exceptions.AllweightsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AllweightsConnect {
    public static final String TAG = AllweightsConnect.class.getSimpleName();

    private ConnectionStatus mConnectionStatus = ConnectionStatus.DISCONNECTED;
    private final ArrayList<OnAllweightsDataListener> mOnAllweightsDataListener;
    private final ArrayList<OnConnectionStatusListener> mOnConectionStatusListener;
    private String mBluetoothDeviceAddress;
    private Integer deviceType;
    private String entrada = "";
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private boolean transmision_activa = false;
    private BluetoothListener listener;
    private BluetoothGatt mBluetoothGatt;
    private final BluetoothManager mBluetoothManager;
    private BluetoothConnectTask taskbluetooth = null;
    TransmisionBluetooth transmisionbluetooth;
    private final Context context;
    private final BluetoothAdapter mBluetoothAdapter;
    private final Handler mMainHandler;
    protected final Object channelsLock = new Object();
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            synchronized (channelsLock) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    newConnectionStatus(ConnectionStatus.CONNECTED);
                    Log.i(TAG, "Connected to GATT server.");
                    // Attempts to discover services after successful connection.
                    Log.i(TAG, "Attempting to start service discovery:" + gatt.discoverServices());
                } else if (newState == BluetoothProfile.STATE_CONNECTING) {
                    Log.i(TAG, "Connecting to GATT server.");
                    newConnectionStatus(ConnectionStatus.CONNECTING);
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "Disconnected from GATT server.");
                    newConnectionStatus(ConnectionStatus.DISCONNECTED);
                } else if (newState == BluetoothProfile.STATE_DISCONNECTING) {
                    Log.i(TAG, "Disconnecting to GATT server.");
                    newConnectionStatus(ConnectionStatus.DISCONNECTING);
                }
            }
        }

        @Override
        @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.w(TAG, "onServicesDiscovered received: " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                displayGattServices(gatt.getServices());
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                procesardatos(characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            procesardatos(characteristic);
        }
    };

    public AllweightsConnect(@NonNull final Context context) {
        mOnAllweightsDataListener = new ArrayList<>();
        mOnConectionStatusListener = new ArrayList<>();
        this.context = context;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        this.mMainHandler = new Handler(Looper.getMainLooper());
    }

    public void setDevice(String deviceAddress, Integer deviceType) {
        this.mBluetoothDeviceAddress = deviceAddress;
        this.deviceType = deviceType;
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    public void setDevice(@NonNull BluetoothDevice device) {
        setDevice(device.getAddress(), device.getType());
    }

    private void newConnectionStatus(ConnectionStatus newConnectionStatus) {
        mMainHandler.post(() -> {
            mConnectionStatus = newConnectionStatus;
            for (OnConnectionStatusListener listener : mOnConectionStatusListener) {
                listener.onConnectionStatus(mConnectionStatus);
            }
        });
    }

    public ConnectionStatus getConnectionStatus() {
        return mConnectionStatus;
    }

    public void addOnConnectionStatusListener(OnConnectionStatusListener listener) {
        mOnConectionStatusListener.add(listener);
    }

    public void removeOnConnectionStatusListener(OnConnectionStatusListener listener) {
        mOnConectionStatusListener.remove(listener);
    }

    public void clearOnConnectionStatusListener() {
        mOnConectionStatusListener.clear();
    }

    public void addOnAllweightsDataListener(OnAllweightsDataListener listener) {
        mOnAllweightsDataListener.add(listener);
    }

    public void removeOnAllweightsDataListener(OnAllweightsDataListener listener) {
        mOnAllweightsDataListener.remove(listener);
    }

    public void clearOnAllweightsDataListener() {
        mOnAllweightsDataListener.clear();
    }

    @RequiresPermission(allOf = {
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT"
    })
    public void connect() throws AllweightsException {
        if (mBluetoothDeviceAddress == null || deviceType == null) {
            throw new AllweightsException("Device not assigned");
        }

        if (this.deviceType == 1) {
            connectBluetoothV1Task();
        } else {
            if (mBluetoothAdapter == null) {
                Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
                return;
            }

            // Previously connected device.  Try to reconnect.
            if (mBluetoothGatt != null) {
                Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");

                mBluetoothGatt.connect();
                //forceConnect();
                sendStateConection();

                return;
            }

            BluetoothDevice mDevice = mBluetoothAdapter.getRemoteDevice(mBluetoothDeviceAddress);
            if (mDevice == null) {
                Log.w(TAG, "Device not found.  Unable to connect.");
                return;
            }
            // We want to directly connect to the device, so we are setting the autoConnect
            // parameter to false.
            mBluetoothGatt = mDevice.connectGatt(context, false, mGattCallback);
            Log.d(TAG, "Trying to create a new connection.");
            sendStateConection();
        }
    }

    @SuppressLint("MissingPermission")
    public void disconnect() {
        if (ConnectionStatus.DISCONNECTED == mConnectionStatus) {
            return;
        }

        if (deviceType == 1) {
            taskbluetooth.finish();
        } else {
            if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return;
            }
            mBluetoothGatt.disconnect();
            sendStateConection();
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    public void destroyService() {
        if (deviceType == 1) {
            if (transmisionbluetooth != null) {
                transmisionbluetooth.cancel(true);
            }
            if (taskbluetooth != null) {
                taskbluetooth.finish();
            }
            taskbluetooth = null;
            if (listener != null) {
                listener.onStatusConnection(ConnectionStatus.DISCONNECTED);
            }
            listener = null;
        } else {
            if (mBluetoothGatt == null) {
                return;
            }
            mBluetoothGatt.close();
            mBluetoothGatt = null;
            sendStateConection();
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    public boolean waxScale() {
        String comando = "0;";
        if (deviceType == 1) {
            return taskbluetooth.sendData(comando);
        }
        return enviarMensaje(comando);
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    public boolean calibrateScale(Integer calibracion) {
        String comando = "a;calibrar;" + calibracion + ";";
        if (deviceType == 1) {
            return taskbluetooth.sendData(comando);
        }
        return enviarMensaje(comando);
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    public boolean sampleQuantity(Integer sampleQuantity) {
        String comando = "velocidad;" + sampleQuantity + ";";
        if (deviceType == 1) {
            return taskbluetooth.sendData(comando);
        }
        return enviarMensaje(comando);
    }

    @RequiresPermission(allOf = {
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT"
    })
    private void connectBluetoothV1Task() {
        newConnectionStatus(ConnectionStatus.CONNECTING);
        if (listener == null) {
            listener = new BluetoothListener() {

                @Override
                public void onStatusConnection(ConnectionStatus status) {
                    newConnectionStatus(status);
                }

                @Override
                public void OnResult(String result) {
                    procesardatos(result);
                }

                @Override
                public void initask(BluetoothSocket btSocket, BluetoothDevice btdevice) {
                    transmisionbluetooth = new TransmisionBluetooth(listener, btSocket);
                    transmisionbluetooth.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                    newConnectionStatus(ConnectionStatus.CONNECTED);
                }
            };
        }
        if (taskbluetooth == null) {
            taskbluetooth = new BluetoothConnectTask(listener, mBluetoothDeviceAddress);
        }
        taskbluetooth.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void procesardatos(@NonNull final BluetoothGattCharacteristic characteristic) {
        synchronized (channelsLock) {
            if (GattAttributes.HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
                int flag = characteristic.getProperties();
                int format;
                if ((flag & 0x01) != 0) {
                    format = BluetoothGattCharacteristic.FORMAT_UINT16;
                    Log.d(TAG, "Heart rate format UINT16.");
                } else {
                    format = BluetoothGattCharacteristic.FORMAT_UINT8;
                    Log.d(TAG, "Heart rate format UINT8.");
                }
                final int heartRate = characteristic.getIntValue(format, 1);
                Log.d(TAG, String.format("Received heart rate: %d", heartRate));
                procesardatos(String.valueOf(heartRate));
            } else {
                // For all other profiles, writes the data formatted in HEX.
                final byte[] data = characteristic.getValue();
                if (data != null && data.length > 0) {
                    procesardatos(new String(data));
                }
            }
        }
    }

    /**
     * @param strReceived data recived bluetooth conection
     */
    private void procesardatos(String strReceived) {
        if (strReceived != null) {
            try {
                entrada = entrada + strReceived;
                String[] cont = entrada.split(":");
                if (cont.length >= 1) {
                    String[] datos = cont[0].split(";");
                    AllweightsData bluetoothDataRecive = new AllweightsData();
                    if (datos.length == 1) {
                        bluetoothDataRecive.weight = Float.parseFloat(datos[0]);
                    } else if (datos.length == 2) {
                        bluetoothDataRecive.weight = Float.parseFloat(datos[0]);
                        bluetoothDataRecive.bateryPercent = Float.parseFloat(datos[1]);
                    } else if (datos.length == 3) {
                        bluetoothDataRecive.weight = Float.parseFloat(datos[0]);
                        bluetoothDataRecive.isEnergyConnected = Objects.equals(datos[1], "1");
                        bluetoothDataRecive.bateryPercent = Float.parseFloat(datos[2]);
                    }
                    entrada = entrada.substring(cont[0].length() + 1);

                    mMainHandler.post(() -> {
                        for (OnAllweightsDataListener listener : mOnAllweightsDataListener) {
                            listener.onAllweightsData(bluetoothDataRecive);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "", e);
                entrada = "";
            }
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        synchronized (channelsLock) {
            for (BluetoothGattService gattService : gattServices) {
                for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                    if (gattCharacteristic.getUuid().equals(GattAttributes.SHOW_DATA)) {
                        activarCaracteristica(gattCharacteristic);
                        return;
                    }
                }
            }
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                               boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (GattAttributes.HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())
                || GattAttributes.SHOW_DATA.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG);

            if (descriptor != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mBluetoothGatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                } else {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(descriptor);
                }
            }
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private void activarCaracteristica(@NonNull BluetoothGattCharacteristic caracteristica) {
        final int charaProp = caracteristica.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            // If there is an active notification on a characteristic, clear
            // it first so it doesn't update the data field on the user interface.
            if (mNotifyCharacteristic != null) {
                setCharacteristicNotification(
                        mNotifyCharacteristic, false);
                mNotifyCharacteristic = null;
            }
            readCharacteristic(caracteristica);
        }
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic = caracteristica;
            setCharacteristicNotification(
                    caracteristica, true);
        }
        activarPeso();
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private void activarPeso() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (comprobarConexionBle()) {
                if (!transmision_activa) {
                    activarCaracteristica(mNotifyCharacteristic);
                    transmision_activa = true;
                }
                sendData("a;", mNotifyCharacteristic);
                Log.i(TAG, "activando");
            } else {
                Log.i(TAG, "Reinicie la balanza");
            }
        }, 1500);
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private boolean enviarMensaje(String mensaje) {
        if (comprobarConexionBle()) {
            return sendData(mensaje, mNotifyCharacteristic);
        }
        return false;
    }

    private boolean comprobarConexionBle() {
        return mBluetoothGatt != null && mNotifyCharacteristic != null;
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private boolean sendData(@NonNull final String action, @NonNull final BluetoothGattCharacteristic caractersitic) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            int result = mBluetoothGatt.writeCharacteristic(caractersitic,
                    action.getBytes(),
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

            return result == BluetoothStatusCodes.SUCCESS;
        } else {
            caractersitic.setValue(action.getBytes());
            return mBluetoothGatt.writeCharacteristic(caractersitic);
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private void sendStateConection() {
        if (mBluetoothManager == null || mBluetoothGatt == null) {
            newConnectionStatus(ConnectionStatus.DISCONNECTED);
            return;
        }
        int state = mBluetoothManager.getConnectionState(mBluetoothGatt.getDevice(), BluetoothProfile.GATT);
        ConnectionStatus action = null;

        if (state == BluetoothProfile.STATE_CONNECTED) {
            action = ConnectionStatus.CONNECTED;
        } else if (state == BluetoothProfile.STATE_CONNECTING) {
            action = ConnectionStatus.CONNECTING;
        } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
            action = ConnectionStatus.DISCONNECTED;
        } else if (state == BluetoothProfile.STATE_DISCONNECTING) {
            action = ConnectionStatus.DISCONNECTING;
        }

        newConnectionStatus(Objects.requireNonNullElse(action, ConnectionStatus.DISCONNECTED));
    }

    public interface OnConnectionStatusListener {
        void onConnectionStatus(ConnectionStatus status);
    }

    public interface OnAllweightsDataListener {
        void onAllweightsData(AllweightsData data);
    }
}
