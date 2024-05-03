package com.ingreatsol.allweights.connect;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.ingreatsol.allweights.common.AllweightsBase;
import com.ingreatsol.allweights.common.AllweightsException;

import java.util.LinkedHashSet;
import java.util.Objects;

public abstract class AllweightsConnect extends AllweightsBase {
    private final BluetoothManager mBluetoothManager;
    private final LinkedHashSet<ConnectionStatusChangeListener> connectionStatusChangeListeners;
    private final LinkedHashSet<DataChangeListener> dataChangeListeners;

    private ConnectionStatus mConnectionStatus = ConnectionStatus.DISCONNECTED;
    private String entrada = "";
    private AllweightsData allweightsData;
    private String mBluetoothDeviceAddress;
    private Integer deviceType;
    private final Object statusLock = new Object();
    private final Object dataLock = new Object();

    public AllweightsConnect(@NonNull final Context context, String feature) {
        super(context, feature);
        connectionStatusChangeListeners = new LinkedHashSet<>();
        dataChangeListeners = new LinkedHashSet<>();
        this.mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
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
        connectionStatusChangeListeners.clear();
        dataChangeListeners.clear();
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    protected boolean sendMessage(String message) {
        Log.d(TAG, "Mesagge send: " + message);
        return false;
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
            synchronized (statusLock) {
                this.mConnectionStatus = newConnectionStatus;
                for (ConnectionStatusChangeListener listener : connectionStatusChangeListeners) {
                    listener.onConnectionStatusChange(mConnectionStatus);
                }
            }
        });
    }

    public synchronized ConnectionStatus getConnectionStatus() {
        return mConnectionStatus;
    }

    public AllweightsData getAllweightsData() {
        return allweightsData;
    }

    protected String getBluetoothDeviceAddress() {
        return mBluetoothDeviceAddress;
    }

    protected Integer getDeviceType() {
        return deviceType;
    }

    protected BluetoothManager getBluetoothManager() {
        return mBluetoothManager;
    }

    public void addOnConnectionStatusChangeListener(ConnectionStatusChangeListener listener) {
        connectionStatusChangeListeners.add(listener);
    }

    public void removeOnConnectionStatusChangeListener(ConnectionStatusChangeListener listener) {
        connectionStatusChangeListeners.remove(listener);
    }

    public void clearOnConnectionStatusChangeListener() {
        connectionStatusChangeListeners.clear();
    }

    public void addOnDataChangeListener(DataChangeListener listener) {
        dataChangeListeners.add(listener);
    }

    public void removeOnDataChangeListener(DataChangeListener listener) {
        dataChangeListeners.remove(listener);
    }

    public void clearOnDataChangeListener() {
        dataChangeListeners.clear();
    }

    /**
     * @param strReceived data recived bluetooth conection
     */
    protected synchronized void procesardatos(String strReceived) {
        if (strReceived != null) {
            try {
                entrada = entrada + strReceived;
                String[] cont = entrada.split(":");
                if (cont.length >= 1) {
                    String[] datos = cont[0].split(";");
                    AllweightsData bluetoothDataRecive = new AllweightsData();
                    if (datos.length >= 1) {
                        bluetoothDataRecive.weight = Float.parseFloat(datos[0]);
                    }
                    if (datos.length == 2) {
                        bluetoothDataRecive.bateryPercent = Float.parseFloat(datos[1]);
                    } else if (datos.length == 3) {
                        bluetoothDataRecive.isEnergyConnected = Objects.equals(datos[1], "1");
                        bluetoothDataRecive.bateryPercent = Float.parseFloat(datos[2]);
                    }
                    entrada = entrada.substring(cont[0].length() + 1);
                    mMainHandler.post(() -> {
                        synchronized (dataLock){
                            this.allweightsData = bluetoothDataRecive;
                            for (DataChangeListener listener : dataChangeListeners) {
                                listener.onDataChange(allweightsData);
                            }
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

    public interface ConnectionStatusChangeListener {
        void onConnectionStatusChange(ConnectionStatus status);
    }

    public interface DataChangeListener {
        void onDataChange(AllweightsData data);
    }
}
