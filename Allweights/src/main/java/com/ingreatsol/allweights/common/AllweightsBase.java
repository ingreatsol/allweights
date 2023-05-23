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

import java.util.Arrays;
import java.util.Objects;

public abstract class AllweightsBase {
    protected static final String TAG = "AllweightsLibrary";

    protected final Context context;
    protected final BluetoothAdapter mBluetoothAdapter;
    protected final Handler mMainHandler;
    protected final Object channelsLock = new Object();
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

        if (!isLocationEnabled()) {
            throw new AllweightsException("Ubicación no habilitada");
        }

        if (isMissingPermisionLocation()) {
            throw new AllweightsException("Faltan permisos de ubicacion");
        }

        if (isMissingPermisionBluetooth()) {
            throw new AllweightsException("Faltan permisos de bluetooth");
        }
    }

    @NonNull
    public Boolean isLocationEnabled() {
        LocationManager lm = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return gps_enabled && network_enabled;
    }

    public Boolean isBluethoothEnabled() {

        boolean bluetooth_enabled = false;

        try {
            bluetooth_enabled = mBluetoothAdapter.isEnabled();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return bluetooth_enabled;
    }



    public boolean isMissingPermisionBluetooth() {
        if (!AllweightsUtils.isRequiredPermisionBluetooth()) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Arrays.stream(AllweightsUtils.Permission.BLUETOOTH).anyMatch(this::checkPermissionsIfNecessary);
        } else {
            for (String permission : AllweightsUtils.Permission.BLUETOOTH) {
                if (checkPermissionsIfNecessary(permission)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean isMissingPermisionLocation() {
        if (!AllweightsUtils.isRequiredPermisionLocation()) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Arrays.stream(AllweightsUtils.Permission.LOCATION).anyMatch(this::checkPermissionsIfNecessary);
        } else {
            for (String permission : AllweightsUtils.Permission.LOCATION) {
                if (checkPermissionsIfNecessary(permission)) {
                    return true;
                }
            }
            return false;
        }
    }

    public boolean checkPermissionsIfNecessary(@NonNull String permissionSend) {
        switch (permissionSend) {
            case "android.permission.ACCESS_FINE_LOCATION":
            case "android.permission.ACCESS_COARSE_LOCATION":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return false;
                break;
            case "android.permission.BLUETOOTH_SCAN":
            case "android.Permission.BLUETOOTH_ADVERTISE":
            case "android.permission.BLUETOOTH_CONNECT":
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false;
                break;
        }

        return (ContextCompat.checkSelfPermission(context, permissionSend) != PackageManager.PERMISSION_GRANTED);
    }
}
