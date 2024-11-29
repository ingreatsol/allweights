package com.ingreatsol.allweights.common;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;

import java.util.Arrays;
import java.util.Objects;

public abstract class AllweightsBase {
    protected static final String TAG = "AllweightsLibrary";

    protected final Context context;
    protected final BluetoothAdapter mBluetoothAdapter;
    protected final Handler mMainHandler;
    protected final String mFeature;

    public AllweightsBase(Context context, String feature) {
        this.context = context;
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mMainHandler = new Handler(Looper.getMainLooper());
        this.mFeature = feature;
    }

    public void destroy() {
        Log.d(TAG, "Destroy " + mFeature);
    }

    public boolean isNotSuportBluetoothConnection() {
        return mBluetoothAdapter == null;
    }

    public boolean isNotExistBluetoothInSystem() {
        return !context.getPackageManager().hasSystemFeature(mFeature);
    }

    protected void checkLocation() throws AllweightsException {
        if (!isLocationEnabled()) {
            throw new AllweightsException("Ubicación no habilitada");
        }

        if (isMissingPermisionLocation()) {
            throw new AllweightsException("Faltan permisos de ubicacion");
        }
    }

    protected void checkBluetooth() throws AllweightsException {
        if (!context.getPackageManager().hasSystemFeature(mFeature) || isNotSuportBluetoothConnection()) {
            if (Objects.equals(mFeature, PackageManager.FEATURE_BLUETOOTH_LE)) {
                throw new AllweightsException("Este teléfono no tiene Bluetooth de baja energía.");
            }

            throw new AllweightsException("Este teléfono no tiene Bluetooth.");
        }

        if (!isBluethoothEnabled()) {
            throw new AllweightsException("Bluetooth no habilitado");
        }

        if (isMissingPermisionBluetooth()) {
            throw new AllweightsException("Faltan permisos de bluetooth");
        }
    }

    @NonNull
    public Boolean isLocationEnabled() {
        return AllweightsUtils.isLocationEnabled(context);
    }

    public Boolean isBluethoothEnabled() {
        return AllweightsUtils.isBluethoothEnabled();
    }

    public boolean isMissingPermisionBluetooth() {
        return AllweightsUtils.isMissingPermisionBluetooth(context);
    }

    public boolean isMissingPermisionLocation() {
        return AllweightsUtils.isMissingPermisionLocation(context);
    }

    public boolean checkPermissionsIfNecessary(@NonNull String permissionSend) {
        return AllweightsUtils.checkPermissionsIfNecessary(permissionSend, context);
    }
}
