package com.ingreatsol.allweights.connect;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.ingreatsol.allweights.common.AllweightsBase;
import com.ingreatsol.allweights.common.AllweightsException;

import java.util.ArrayList;
import java.util.Objects;

public abstract class AllweightsConnect extends AllweightsBase {
    protected ConnectionStatus mConnectionStatus = ConnectionStatus.DISCONNECTED;
    protected final ArrayList<AllweightsConnectCallback> mOnAllweightsConnectCallback;
    protected String mBluetoothDeviceAddress;
    protected Integer deviceType;
    protected String entrada = "";
    protected final BluetoothManager mBluetoothManager;

    public AllweightsConnect(@NonNull final Context context, String feature) {
        super(context, feature);
        mOnAllweightsConnectCallback = new ArrayList<>();
        this.mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    public void setDevice(String deviceAddress, Integer deviceType) {
        this.mBluetoothDeviceAddress = deviceAddress;
        this.deviceType = deviceType;
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    public void setDevice(@NonNull BluetoothDevice device) {
        setDevice(device.getAddress(), device.getType());
    }

    protected void newConnectionStatus(ConnectionStatus newConnectionStatus) {
        mMainHandler.post(() -> {
            mConnectionStatus = newConnectionStatus;
            for (AllweightsConnectCallback listener : mOnAllweightsConnectCallback) {
                listener.onConnectionStatusChange(mConnectionStatus);
            }
        });
    }

    public synchronized ConnectionStatus getConnectionStatus() {
        return mConnectionStatus;
    }

    public void addOnAllweightsConnectCallback(AllweightsConnectCallback listener) {
        mOnAllweightsConnectCallback.add(listener);
    }

    public void removeOnAllweightsConnectCallback(AllweightsConnectCallback listener) {
        mOnAllweightsConnectCallback.remove(listener);
    }

    public void clearOnAllweightsConnectCallback() {
        mOnAllweightsConnectCallback.clear();
    }

    /**
     * @param strReceived data recived bluetooth conection
     */
    protected void procesardatos(String strReceived) {
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
                        for (AllweightsConnectCallback listener : mOnAllweightsConnectCallback) {
                            listener.onAllweightsDataChange(bluetoothDataRecive);
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
    public boolean activateWeight() {
        return sendMessage("a;");
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    public boolean sampleQuantity(Integer sampleQuantity) {
        return sendMessage("velocidad;" + sampleQuantity + ";");
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    public boolean calibrateScale(Integer calibracion) {
        return sendMessage("a;calibrar;" + calibracion + ";");
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    public boolean waxScale() {
        return sendMessage("0;");
    }

    public synchronized void connect() throws AllweightsException {
        checkBluetooth();

        if (mBluetoothDeviceAddress == null || deviceType == null) {
            throw new AllweightsException("Device not assigned");
        }
    }

    public synchronized void disconnect() {
        Log.d(TAG, "Disconnected " + mFeature);
    }

    public void destroy() {
        Log.d(TAG, "destroy");
        mBluetoothDeviceAddress = null;
        deviceType = null;
        entrada = null;
        mConnectionStatus = ConnectionStatus.DISCONNECTED;
        mOnAllweightsConnectCallback.clear();
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    protected boolean sendMessage(String message) {
        Log.d(TAG, "Mesagge send: " + message);
        return false;
    }
}
