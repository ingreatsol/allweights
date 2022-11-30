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
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class BluetoothViewModel extends ViewModel {
    private final MutableLiveData<String> dataRecived;
    private final MutableLiveData<Integer> porcentaje;
    private final MutableLiveData<Boolean> isConnected;
    private final MutableLiveData<String> progrescolor;
    private final MutableLiveData<String> mensaje;
    private String mDeviceAddress;
    public Integer type;
    public BluetoothLeService mBluetoothLeService;
    public BluetoothGattCharacteristic mNotifyCharacteristic;
    boolean mConnected = false;
    private String entrada = "";
    private boolean transmision_activa = false;
    private Bluetooth_listener listener;
    public Bluetooth taskbluetooth = null;
    Transmision_bluetooth transmisionbluetooth;


    public BluetoothViewModel() {
        porcentaje = new MutableLiveData<>(0);
        dataRecived = new MutableLiveData<>();
        isConnected = new MutableLiveData<>(false);
        progrescolor = new MutableLiveData<>("#FFFFFF");
        mensaje = new MutableLiveData<>();
    }

    public void init(String mDeviceAddress, Integer type, Activity activity) {
        this.mDeviceAddress = mDeviceAddress;
        this.type = type;
        if(type == 1)
        {
            listener = new Bluetooth_listener() {
                @Override
                public void onFinisched() {
                    isConnected.postValue(false);
                    activity.finish();
                }

                @Override
                public void OnResult(String result) {
                    displayData(result);
                }

                @Override
                public void initask(BluetoothSocket btSocket, BluetoothDevice btdevice) {
                    transmisionbluetooth = new Transmision_bluetooth(listener, btSocket);
                    transmisionbluetooth.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    isConnected.postValue(true);
                }
            };
            taskbluetooth = new Bluetooth(listener, mDeviceAddress);
            taskbluetooth.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        }else {
            Intent gattServiceIntent = new Intent(activity, BluetoothLeService.class);
            activity.bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        }

    }

    public LiveData<String> getDataRecived() {
        return dataRecived;
    }

    public LiveData<Boolean> getIsConnected() {
        return isConnected;
    }

    public LiveData<Integer> getPorcentaje(){
        return porcentaje;
    }

    public LiveData<String> getProgresColor(){
        return progrescolor;
    }

    public final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                isConnected.postValue(true);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                isConnected.postValue(false );
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void displayData(String data) {
        if (data != null) {
            dataRecived.setValue(data);
            //procesardatos(data);
        }
    }

    public void connectBleService() {
        if(type == 1)
        {

        }else {
            if (mBluetoothLeService != null) {
                mBluetoothLeService.connect(mDeviceAddress);
            }
        }
    }

    public final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
               // activity.finish();
            }
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    public final IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

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
                if(uuid.equals("0000ffe1-0000-1000-8000-00805f9b34fb"))
                {
                    retornar_caracteristica(gattCharacteristic);
                }
            }
        }
    }

    public boolean retornar_caracteristica(BluetoothGattCharacteristic caracteristica){
        final BluetoothGattCharacteristic characteristic = caracteristica;
        final int charaProp = characteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            // If there is an active notification on a characteristic, clear
            // it first so it doesn't update the data field on the user interface.
            if (mNotifyCharacteristic != null) {
                mBluetoothLeService.setCharacteristicNotification(
                        mNotifyCharacteristic, false);
                mNotifyCharacteristic = null;
            }
            mBluetoothLeService.readCharacteristic(characteristic);
        }
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic = characteristic;
            mBluetoothLeService.setCharacteristicNotification(
                    characteristic, true);
        }
        activar_peso();
        return true;
    }

    public void activar_peso(){
        new Handler().postDelayed(() -> {
            if(mBluetoothLeService != null) {
                if(mNotifyCharacteristic != null){
                    if(!transmision_activa){
                        retornar_caracteristica(mNotifyCharacteristic);
                        transmision_activa = true;
                    }
                    mBluetoothLeService.recibir_datos("a;", mNotifyCharacteristic);
                    mBluetoothLeService.recibir_datos("a;", mNotifyCharacteristic);
                    mensaje.setValue("activando");
                }
                else{
                    mensaje.setValue("Reinicie la balanza");
                }
            }else{
                mensaje.setValue("Reinicie la balanza");
            }
        }, 1500);

    }

    public void enviar_mensaje(String mensaje) {
        if (comprobar_conexionBle()) {
            mBluetoothLeService.recibir_datos(mensaje, mNotifyCharacteristic);
            //Toast.makeText(activity, mensaje, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean comprobar_conexionBle(){
        if(mBluetoothLeService != null) {
            if (mNotifyCharacteristic != null) {
                return true;
            }else{
                mensaje.setValue("Reinicie la balanza");
                return false;
            }
        }else{
            mensaje.setValue("Reinicie la balanza");
            return false;
        }
    }

    public void registar_sevicio(Activity activity){
        activity.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            mBluetoothLeService.connect(mDeviceAddress);
        }
    }

    public void quitar_servicio(Activity activity){
        activity.unregisterReceiver(mGattUpdateReceiver);
    }

    public void eliminar_servicio(Activity activity){
        if(this.type == 1) {
            if (transmisionbluetooth != null) {
                transmisionbluetooth.isCancelled();
            }
            if (taskbluetooth != null) {
                taskbluetooth.finish();
            }
        }else{
            activity.unbindService(mServiceConnection);
            mBluetoothLeService = null;
        }
    }

    public void encerar_balanza()
    {
        if(mBluetoothLeService != null) {
            if(mNotifyCharacteristic != null){
                mBluetoothLeService.recibir_datos("0;", mNotifyCharacteristic);
            }
            else{
                mensaje.setValue("Reinicie la balanza");
            }
        }else{
            mensaje.setValue("Reinicie la balanza");
        }
    }

}
