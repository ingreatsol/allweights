package com.ingreatsol.allweights;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.ChecksSdkIntAtLeast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class AllweightsUtils {

    public static final float RANGO_MAXIMO_BATERIA = 4.2f;
    public static final float RANGO_MINIMO_BATERIA = 3.2f;
    public static final float LIMITE_BATERIA = RANGO_MAXIMO_BATERIA - RANGO_MINIMO_BATERIA;
    public static final String TAG = AllweightsUtils.class.getSimpleName();

    public static final class Permission {
        public static final String[] LOCATION = new String[]{
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION"};
        public static final String[] BLUETOOTH = new String[]{
                "android.permission.BLUETOOTH_SCAN",
                "android.permission.BLUETOOTH_CONNECT"};
    }

    public static boolean checkPermissionsIfNecessary(Context context,
                                                      @NonNull String permissionSend) {
        switch (permissionSend) {
            case "android.permission.ACCESS_FINE_LOCATION":
            case "android.permission.ACCESS_COARSE_LOCATION":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return false;
            case "android.permission.BLUETOOTH_SCAN":
            case "android.Permission.BLUETOOTH_ADVERTISE":
            case "android.permission.BLUETOOTH_CONNECT":
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false;
        }
        return (ContextCompat.checkSelfPermission(context, permissionSend) != PackageManager.PERMISSION_GRANTED);
    }

    @NonNull
    public static ArrayList<String> getPermissionEscanearBluetooth() {
        ArrayList<String> list = new ArrayList<>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            list.addAll(Arrays.asList(Permission.LOCATION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            list.addAll(Arrays.asList(Permission.BLUETOOTH));
        }
        return list;
    }

    public static boolean isMissingPermisionsEscanearBluetooth(Context application) {
        return isMissingPermisionLocation(application) && isMissingPermisionBluetooth(application);
    }

    public static boolean isRequiredPermisionLocation() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S;
    }

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
    public static boolean isRequiredPermisionBluetooth() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }

    public static boolean isMissingPermisionLocation(Context context) {
        if (!isRequiredPermisionLocation()) {
            return true;
        }

        return Arrays.stream(Permission.LOCATION)
                .noneMatch(per -> checkPermissionsIfNecessary(context, per));
    }

    public static boolean isMissingPermisionBluetooth(Context context) {
        if (!isRequiredPermisionBluetooth()) {
            return true;
        }

        return Arrays.stream(Permission.BLUETOOTH)
                .noneMatch(per -> checkPermissionsIfNecessary(context, per));
    }

    @NonNull
    public static ArrayList<String> shouldMapPermission(FragmentActivity activity, @NonNull String... permissionSend) {
        ArrayList<String> permissionsShoulShow = new ArrayList<>();
        for (String permission : permissionSend) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                permissionsShoulShow.add(permission);
            }
        }
        return permissionsShoulShow;
    }

    @NonNull
    public static Boolean isLocationEnabled(@NonNull Context activity) {
        LocationManager lm = (LocationManager)
                activity.getSystemService(Context.LOCATION_SERVICE);
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

    public Boolean isBluethoothEnabled(@NonNull Context activity) {
        BluetoothManager bluetoothManager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);

        boolean bluetooth_enabled = false;

        try {
            bluetooth_enabled = bluetoothManager.getAdapter().isEnabled();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return bluetooth_enabled;
    }
}
