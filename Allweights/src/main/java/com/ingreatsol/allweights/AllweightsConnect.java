package com.ingreatsol.allweights;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AllweightsConnect {
    public static final String TAG = AllweightsScan.class.getSimpleName();

    private final MutableLiveData<AllweightsData> data;
    private final MutableLiveData<EstadoConexion> isConnected;
    private BluetoothDevice device;
    private String entrada = "";
    public AllweightsBluetoothLeService mBluetoothLeService;
    private BluetoothAdapter mBluetoothAdapter;
    public BluetoothGattCharacteristic mNotifyCharacteristic;
    private boolean transmision_activa = false;
    private Bluetooth_listener listener;
    public Bluetooth taskbluetooth = null;
    Transmision_bluetooth transmisionbluetooth;


    public AllweightsConnect() {
        data = new MutableLiveData<>();
        isConnected = new MutableLiveData<>(EstadoConexion.DESCONECTADO);
    }

    public MutableLiveData<AllweightsData> getData() {
        return data;
    }

    public MutableLiveData<EstadoConexion> getIsConnected() {
        return isConnected;
    }

    @RequiresPermission(allOf = {
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT"
    })
    public void init(FragmentActivity activity, @NonNull BluetoothDevice device) {
        this.device = device;
        if (device.getType() == 1) {
            listener = new Bluetooth_listener() {
                @Override
                public void onFinisched() {
                    isConnected.postValue(EstadoConexion.DESCONECTADO);
                }

                @Override
                public void OnResult(String result) {
                    procesardatos(result);
                }

                @Override
                public void initask(BluetoothSocket btSocket, BluetoothDevice btdevice) {
                    transmisionbluetooth = new Transmision_bluetooth(listener, btSocket);
                    transmisionbluetooth.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    isConnected.postValue(EstadoConexion.CONECTADO);
                }
            };
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
    private void connectBluetoothV1Task() {
        isConnected.postValue(EstadoConexion.CONECTANDO);
        if (taskbluetooth == null) {
            taskbluetooth = new Bluetooth(listener, device.getAddress());
        }
        taskbluetooth.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
        public void onReceive(Context context, @NonNull Intent intent) {
            final String action = intent.getAction();
            switch (action) {
                case AllweightsBluetoothLeService.ACTION_GATT_CONNECTED:
                    isConnected.postValue(EstadoConexion.CONECTADO);
                    break;
                case AllweightsBluetoothLeService.ACTION_GATT_CONNECTING:
                    isConnected.postValue(EstadoConexion.CONECTANDO);
                    break;
                case AllweightsBluetoothLeService.ACTION_GATT_DISCONNECTED:
                    isConnected.postValue(EstadoConexion.DESCONECTADO);
                    break;
                case AllweightsBluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED:
                    displayGattServices(AllweightsBluetoothLeService.getInstance().getSupportedGattServices());
                    break;
                case AllweightsBluetoothLeService.ACTION_DATA_AVAILABLE:
                    procesardatos(intent.getStringExtra(AllweightsBluetoothLeService.EXTRA_DATA));
                    break;
                default:
                    break;
            }
        }
    };

    private void procesardatos(String strReceived) {
        if (strReceived != null) {
            try {
                entrada = entrada + strReceived;
                String[] cont = entrada.split(":");
                if (cont.length >= 1) {
                    String[] datos = cont[0].split(";");
                    AllweightsData bluetoothDataRecive = new AllweightsData();
                    if (datos.length == 1) {
                        bluetoothDataRecive.peso = Float.parseFloat(datos[0]);
                    } else if (datos.length == 2) {
                        bluetoothDataRecive.peso = Float.parseFloat(datos[0]);
                        bluetoothDataRecive.porcentaje_bateria = Float.parseFloat(datos[1]);
                    } else if (datos.length == 3) {
                        bluetoothDataRecive.peso = Float.parseFloat(datos[0]);
                        bluetoothDataRecive.carga = Objects.equals(datos[1], "1");
                        bluetoothDataRecive.porcentaje_bateria = Float.parseFloat(datos[2]);
                    }
                    entrada = entrada.substring(cont[0].length() + 1);
                    data.setValue(bluetoothDataRecive);
                }
            } catch (Exception ignored) {
                entrada = "";
            }
        }
    }

    @RequiresPermission(allOf = {
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT"
    })
    public void connectBleService() {
        if (device.getType() == 1) {
            connectBluetoothV1Task();
        } else {
            if (AllweightsBluetoothLeService.isInstanceCreated()) {
                AllweightsBluetoothLeService.getInstance().connect(device.getAddress());
            }
        }
    }

    public final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((AllweightsBluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                // activity.finish();
            }
            mBluetoothLeService.connect(device.getAddress());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @NonNull
    public final IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AllweightsBluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(AllweightsBluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(AllweightsBluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(AllweightsBluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    public void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<>();
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                uuid = gattCharacteristic.getUuid().toString();
                //aqui voy hacer pruebas
                if (uuid.equals("0000ffe1-0000-1000-8000-00805f9b34fb")) {
                    retornar_caracteristica(gattCharacteristic);
                }
            }
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    public void retornar_caracteristica(@NonNull BluetoothGattCharacteristic caracteristica) {
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
        activar_peso();
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private void activar_peso() {
        new Handler().postDelayed(() -> {
            if (mBluetoothLeService != null) {
                if (mNotifyCharacteristic != null) {
                    if (!transmision_activa) {
                        retornar_caracteristica(mNotifyCharacteristic);
                        transmision_activa = true;
                    }
                    mBluetoothLeService.sendData("a;", mNotifyCharacteristic);
                    Log.i(TAG, "activando");
                } else {
                    Log.i(TAG, "Reinicie la balanza");
                }
            } else {
                Log.i(TAG, "Reinicie la balanza");
            }
        }, 1500);

    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    private void enviar_mensaje(String mensaje) throws AllweightsException {
        if (comprobar_conexionBle()) {
            mBluetoothLeService.sendData(mensaje, mNotifyCharacteristic);
        }
        throw new AllweightsException("Reinicie la balanza");
    }

    public boolean comprobar_conexionBle() {
        if (AllweightsBluetoothLeService.isInstanceCreated()) return mNotifyCharacteristic != null;
        return false;
    }

    @RequiresPermission(allOf = {
            "android.permission.BLUETOOTH_SCAN",
            "android.permission.BLUETOOTH_CONNECT"
    })
    public void registar_sevicio(@NonNull Activity activity) {
        activity.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        connectBleService();
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    public void quitar_servicio(@NonNull Activity activity) {
        activity.unregisterReceiver(mGattUpdateReceiver);
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    public void eliminar_servicio(Activity activity) {
        if (this.device.getType() == 1) {
            if (transmisionbluetooth != null) {
                transmisionbluetooth.isCancelled();
            }
            if (taskbluetooth != null) {
                taskbluetooth.finish();
            }
        } else {
            activity.unbindService(mServiceConnection);
            mBluetoothLeService = null;
        }
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    public void encerar_balanza() throws AllweightsException {
        enviar_mensaje("0;");
    }

    @RequiresPermission("android.permission.BLUETOOTH_CONNECT")
    public void calibrar_balanza(Integer calibracion) throws AllweightsException {
        enviar_mensaje("a;calibrar;" + calibracion + ";");
    }

    public BluetoothAdapter getmBluetoothAdapter(Activity activity) {
        if (mBluetoothAdapter == null) {
            final BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }
        return mBluetoothAdapter;
    }
}
