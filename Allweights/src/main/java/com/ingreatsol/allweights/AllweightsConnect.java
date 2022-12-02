package com.ingreatsol.allweights;

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

import java.util.List;
import java.util.Objects;

public class AllweightsConnect {
    public static final String TAG = AllweightsScan.class.getSimpleName();

    private final MutableLiveData<AllweightsData> data;
    private final MutableLiveData<ConnectionStatus> connectionStatus;
    private String deviceAddress;
    private Integer deviceType;
    private String entrada = "";
    public AllweightsBluetoothLeService mBluetoothLeService;
    public BluetoothGattCharacteristic mNotifyCharacteristic;
    private boolean transmision_activa = false;
    private Bluetooth_listener listener;
    public Bluetooth taskbluetooth = null;
    Transmision_bluetooth transmisionbluetooth;
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
                case GattAttributes.ACTION_GATT_SERVICES_DISCOVERED:
                    displayGattServices(AllweightsBluetoothLeService.getInstance().getSupportedGattServices());
                    break;
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

    public MutableLiveData<AllweightsData> getData() {
        return data;
    }

    public MutableLiveData<ConnectionStatus> getConnectionStatus() {
        return connectionStatus;
    }

    @RequiresPermission(allOf = {
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT"
    })
    public void init(FragmentActivity activity, @NonNull BluetoothDevice device) {
        this.deviceAddress = device.getAddress();
        this.deviceType = device.getType();
        _init(activity);
    }

    @RequiresPermission(allOf = {
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT"
    })
    public void init(FragmentActivity activity, String deviceAddress, Integer deviceType) {
        this.deviceAddress = deviceAddress;
        this.deviceType = deviceType;
        _init(activity);
    }

    @RequiresPermission(allOf = {
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT"
    })
    private void _init(FragmentActivity activity){
        if (this.deviceType == 1) {
            connectBluetoothV1Task();
        } else {
            Intent gattServiceIntent = new Intent(activity, AllweightsBluetoothLeService.class);
            activity.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @RequiresPermission(allOf = {
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT"
    })
    public void registerService(@NonNull Activity activity) {
        activity.registerReceiver(mGattUpdateReceiver, GattAttributes.makeGattUpdateIntentFilter());
        if (deviceType == 1) {
            connectBluetoothV1Task();
        } else {
            if (AllweightsBluetoothLeService.isInstanceCreated()) {
                AllweightsBluetoothLeService.getInstance().connect(deviceAddress);
            }
        }
    }

    @RequiresPermission(allOf = {
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT"
    })
    public void unRegisterService(@NonNull Activity activity) {
        activity.unregisterReceiver(mGattUpdateReceiver);
        if (deviceType == 1) {
            taskbluetooth.finish();
        } else {
            if (AllweightsBluetoothLeService.isInstanceCreated()) {
                AllweightsBluetoothLeService.getInstance().disconnect();
            }
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    public void destroyService(Activity activity) {
        if (deviceType == 1) {
            if (transmisionbluetooth != null) {
                transmisionbluetooth.isCancelled();
            }
            if (taskbluetooth != null) {
                taskbluetooth.finish();
            }
            taskbluetooth = null;
            if (listener != null){
                listener.onFinisched();
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

    @RequiresPermission(allOf = {
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT"
    })
    private void connectBluetoothV1Task() {
        connectionStatus.postValue(ConnectionStatus.CONNECTING);
        if (listener == null){
            listener = new Bluetooth_listener() {
                @Override
                public void onFinisched() {
                    connectionStatus.postValue(ConnectionStatus.DISCONNECTED);
                }

                @Override
                public void OnResult(String result) {
                    procesardatos(result);
                }

                @Override
                public void initask(BluetoothSocket btSocket, BluetoothDevice btdevice) {
                    transmisionbluetooth = new Transmision_bluetooth(listener, btSocket);
                    transmisionbluetooth.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    connectionStatus.postValue(ConnectionStatus.CONNECTED);
                }
            };
        }
        if (taskbluetooth == null) {
            taskbluetooth = new Bluetooth(listener, deviceAddress);
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
            } catch (Exception ignored) {
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
        if (AllweightsBluetoothLeService.isInstanceCreated()) return mNotifyCharacteristic != null;
        return false;
    }
}
