package com.ingreatsol.allweights.connect;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothStatusCodes;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.ingreatsol.allweights.common.AllweightsException;
import com.ingreatsol.allweights.common.GattAttributes;

import java.lang.reflect.Method;
import java.util.List;

public final class AllweightsBleConnect extends AllweightsConnect {

    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private boolean transmision_activa = false;
    private BluetoothGatt mBluetoothGatt;

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
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

    public AllweightsBleConnect(@NonNull final Context context) {
        super(context, PackageManager.FEATURE_BLUETOOTH_LE);
    }

    @RequiresPermission(allOf = {
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT",
            "android.permission.BLUETOOTH_ADMIN"
    })
    @Override
    public void connect() throws AllweightsException {
        super.connect();

        // Previously connected device.  Try to reconnect.
        if (mBluetoothGatt != null) {
            Log.d(TAG, "Trying to force connection: " + getBluetoothDeviceAddress());
            mBluetoothGatt.disconnect();
            mBluetoothGatt = null;
        }

        BluetoothDevice mDevice = mBluetoothAdapter.getRemoteDevice(getBluetoothDeviceAddress());
        if (mDevice == null) {
            Log.w(TAG, "Device not found. Unable to connect.");
            return;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = mDevice.connectGatt(context, false, mGattCallback, BluetoothDevice.TRANSPORT_LE);
        Log.d(TAG, "Trying to create a new connection.");
        var refreshResult = refreshDeviceCache(mBluetoothGatt);
        Log.d(TAG, "BluetoothGatt invoquing refresh result is: " + refreshResult);
        sendStateConection();

        if (isDeviceBonded(mDevice)) {
            if (!unBondDevice(mDevice)) {
                Log.w(TAG, "Device unbond failed");
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void disconnect() {
        super.disconnect();
        if (ConnectionStatus.DISCONNECTED == getConnectionStatus()) {
            return;
        }
        mBluetoothGatt.disconnect();
        sendStateConection();
    }

    @SuppressLint("MissingPermission")
    public void destroy() {
        super.destroy();
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        sendStateConection();
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private boolean isDeviceBonded(BluetoothDevice mDevice) {
        boolean bonded = false;

        for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices()) {
            if (device.getAddress().equals(mDevice.getAddress())) {
                bonded = true;
                break;
            }
        }

        return bonded;
    }

    @RequiresPermission("android.permission.BLUETOOTH_ADMIN")
    private boolean unBondDevice(BluetoothDevice device) {
        try {
            //noinspection JavaReflectionMemberAccess
            Method m = device.getClass()
                    .getMethod("removeBond");
            //noinspection DataFlowIssue
            return (Boolean) m.invoke(device);
        } catch (Exception e) {
            Log.e(TAG, "unBondDevice", e);
            return false;
        }
    }

    private boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            //noinspection JavaReflectionMemberAccess
            Method localMethod = gatt.getClass().getMethod("refresh");
            //noinspection DataFlowIssue
            return (Boolean) localMethod.invoke(gatt);
        } catch (Exception localException) {
            Log.e("ble", "An exception occured while refreshing device");
            return false;
        }
    }

    private void procesardatos(@NonNull final BluetoothGattCharacteristic characteristic) {
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

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        for (BluetoothGattService gattService : gattServices) {
            for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                if (gattCharacteristic.getUuid().equals(GattAttributes.SHOW_DATA)) {
                    activarCaracteristica(gattCharacteristic);
                    return;
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
        mMainHandler.postDelayed(() -> {
            if (comprobarConexionBle()) {
                if (!transmision_activa) {
                    activarCaracteristica(mNotifyCharacteristic);
                    transmision_activa = true;
                }
                sendMessage("a;");
                Log.i(TAG, "activando");
            } else {
                Log.i(TAG, "Reinicie la balanza");
            }
        }, 1500);
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    @Override
    protected boolean sendMessage(String message) {
        if (comprobarConexionBle()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                int result = mBluetoothGatt.writeCharacteristic(mNotifyCharacteristic,
                        message.getBytes(),
                        BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

                return result == BluetoothStatusCodes.SUCCESS;
            } else {
                mNotifyCharacteristic.setValue(message.getBytes());
                return mBluetoothGatt.writeCharacteristic(mNotifyCharacteristic);
            }
        }
        return super.sendMessage(message);
    }

    private boolean comprobarConexionBle() {
        return mBluetoothGatt != null && mNotifyCharacteristic != null;
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private void sendStateConection() {
        if (getBluetoothManager() == null || mBluetoothGatt == null) {
            newConnectionStatus(ConnectionStatus.DISCONNECTED);
            return;
        }
        int state = getBluetoothManager().getConnectionState(mBluetoothGatt.getDevice(), BluetoothProfile.GATT);
        ConnectionStatus action = ConnectionStatus.DISCONNECTED;

        if (state == BluetoothProfile.STATE_CONNECTED) {
            action = ConnectionStatus.CONNECTED;
        } else if (state == BluetoothProfile.STATE_CONNECTING) {
            action = ConnectionStatus.CONNECTING;
        } else if (state == BluetoothProfile.STATE_DISCONNECTING) {
            action = ConnectionStatus.DISCONNECTING;
        }

        newConnectionStatus(action);
    }
}
