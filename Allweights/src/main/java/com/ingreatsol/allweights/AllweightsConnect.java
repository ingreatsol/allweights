package com.ingreatsol.allweights;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;

import com.ingreatsol.allweights.exceptions.AllweightsException;

import java.util.List;
import java.util.Objects;

public class AllweightsConnect {
    public static final String TAG = AllweightsConnect.class.getSimpleName();

    private final MutableLiveData<AllweightsData> data;
    private final MutableLiveData<ConnectionStatus> connectionStatus;
    private String deviceAddress;
    private Integer deviceType;
    private String entrada = "";
    public AllweightsBluetoothLeService mBluetoothLeService;
    public BluetoothGattCharacteristic mNotifyCharacteristic;
    private boolean transmision_activa = false;
    private BluetoothListener listener;
    public BluetoothConnectTask taskbluetooth = null;
    TransmisionBluetooth transmisionbluetooth;
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
        public void onReceive(Context context, @NonNull Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case GattAttributes.ACTION_GATT_CONNECTED:
                    connectionStatus.postValue(ConnectionStatus.CONNECTED);
                    break;
                case GattAttributes.ACTION_GATT_CONNECTING:
                    connectionStatus.postValue(ConnectionStatus.CONNECTING);
                    break;
                case GattAttributes.ACTION_GATT_DISCONNECTED:
                    connectionStatus.postValue(ConnectionStatus.DISCONNECTED);
                    break;
                case GattAttributes.ACTION_GATT_SERVICES_DISCOVERED: {
                    if (mBluetoothLeService != null) {
                        displayGattServices(mBluetoothLeService.getSupportedGattServices());
                    }
                    break;
                }
                case GattAttributes.ACTION_DATA_AVAILABLE:
                    procesardatos(intent.getStringExtra(GattAttributes.EXTRA_DATA));
                    break;
                default:
                    break;
            }
        }
    };
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((AllweightsBluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                return;
            }
            mBluetoothLeService.connect(deviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    public AllweightsConnect() {
        data = new MutableLiveData<>();
        connectionStatus = new MutableLiveData<>(ConnectionStatus.DISCONNECTED);
    }

    public void setDevice(String deviceAddress, Integer deviceType) {
        this.deviceAddress = deviceAddress;
        this.deviceType = deviceType;
    }

    public MutableLiveData<AllweightsData> getData() {
        return data;
    }

    public MutableLiveData<ConnectionStatus> getConnectionStatus() {
        return connectionStatus;
    }

    public void registerService(@NonNull Context context) {
        context.registerReceiver(mGattUpdateReceiver, GattAttributes.makeGattUpdateIntentFilter());
    }

    @RequiresPermission(allOf = {
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT"
    })
    public void connect(@NonNull FragmentActivity activity) throws AllweightsException {
        if (deviceAddress == null || deviceType == null) {
            throw new AllweightsException("Device not assigned");
        }

        if (ConnectionStatus.CONNECTED == connectionStatus.getValue()) {
            return;
        }

        if (this.deviceType == 1) {
            connectBluetoothV1Task();
        } else {
            if (mBluetoothLeService == null){
                Intent gattServiceIntent = new Intent(activity, AllweightsBluetoothLeService.class);
                activity.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
            }else {
                mBluetoothLeService.connect(deviceAddress);
            }
        }
    }

    public void unRegisterService(@NonNull Activity activity) {
        activity.unregisterReceiver(mGattUpdateReceiver);
    }

    @SuppressLint("MissingPermission")
    public void disconnect() {
        if (ConnectionStatus.DISCONNECTED == connectionStatus.getValue()) {
            return;
        }

        if (deviceType == 1) {
            taskbluetooth.finish();
        } else {
            if (mBluetoothLeService != null){
                mBluetoothLeService.disconnect();
            }
        }
    }

    public void destroyService(Activity activity) {
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
            activity.unbindService(mServiceConnection);
            mBluetoothLeService = null;
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
        connectionStatus.postValue(ConnectionStatus.CONNECTING);
        if (listener == null) {
            listener = new BluetoothListener() {

                @Override
                public void onStatusConnection(ConnectionStatus status) {
                    connectionStatus.postValue(status);
                }

                @Override
                public void OnResult(String result) {
                    procesardatos(result);
                }

                @Override
                public void initask(BluetoothSocket btSocket, BluetoothDevice btdevice) {
                    transmisionbluetooth = new TransmisionBluetooth(listener, btSocket);
                    transmisionbluetooth.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    connectionStatus.postValue(ConnectionStatus.CONNECTED);
                }
            };
        }
        if (taskbluetooth == null) {
            taskbluetooth = new BluetoothConnectTask(listener, deviceAddress);
        }
        taskbluetooth.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

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
                    data.setValue(bluetoothDataRecive);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                entrada = "";
            }
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        for (BluetoothGattService gattService : gattServices) {
            for (BluetoothGattCharacteristic gattCharacteristic : gattService.getCharacteristics()) {
                if (gattCharacteristic.getUuid().equals(GattAttributes.HEART_RATE_MEASUREMENT2)) {
                    activarCaracteristica(gattCharacteristic);
                    return;
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
                mBluetoothLeService.setCharacteristicNotification(
                        mNotifyCharacteristic, false);
                mNotifyCharacteristic = null;
            }
            mBluetoothLeService.readCharacteristic(caracteristica);
        }
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic = caracteristica;
            mBluetoothLeService.setCharacteristicNotification(
                    caracteristica, true);
        }
        activarPeso();
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private void activarPeso() {
        new Handler().postDelayed(() -> {
            if (comprobarConexionBle()) {
                if (!transmision_activa) {
                    activarCaracteristica(mNotifyCharacteristic);
                    transmision_activa = true;
                }
                mBluetoothLeService.sendData("a;", mNotifyCharacteristic);
                Log.i(TAG, "activando");
            } else {
                Log.i(TAG, "Reinicie la balanza");
            }
        }, 1500);
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private boolean enviarMensaje(String mensaje) {
        if (comprobarConexionBle()) {
            return mBluetoothLeService.sendData(mensaje, mNotifyCharacteristic);
        }
        return false;
    }

    private boolean comprobarConexionBle() {
        return mBluetoothLeService != null && mNotifyCharacteristic != null;
    }
}
