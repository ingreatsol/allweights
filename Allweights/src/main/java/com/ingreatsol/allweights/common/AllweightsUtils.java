package com.ingreatsol.allweights.common;

import static com.ingreatsol.allweights.common.AllweightsBase.TAG;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class AllweightsUtils {

    public static final float RANGO_MAXIMO_BATERIA = 4.2f;
    public static final float RANGO_MINIMO_BATERIA = 3.2f;
    public static final float LIMITE_BATERIA = RANGO_MAXIMO_BATERIA - RANGO_MINIMO_BATERIA;

    public static final class Permission {
        @NonNull
        public static String[] LOCATION() {
            var permissions = new ArrayList<String>();

            if (isRequiredPermisionLocation()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    permissions.add("android.permission.ACCESS_FINE_LOCATION");
                }
                permissions.add("android.permission.ACCESS_COARSE_LOCATION");
            }
            return permissions.toArray(new String[0]);
        }

        public static final String[] BLUETOOTH = new String[]{
                "android.permission.BLUETOOTH_SCAN",
                "android.permission.BLUETOOTH_CONNECT"};
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

    public static boolean isRequiredPermisionLocation() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S;
    }

    public static boolean isNotRequiredPermisionBluetooth() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S;
    }

    public static boolean isMissingPermisionBluetooth(Context context) {
        if (AllweightsUtils.isNotRequiredPermisionBluetooth()) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Arrays.stream(AllweightsUtils.Permission.BLUETOOTH)
                    .anyMatch(permission -> checkPermissionsIfNecessary(permission, context));
        } else {
            for (String permission : AllweightsUtils.Permission.BLUETOOTH) {
                if (checkPermissionsIfNecessary(permission, context)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isMissingPermisionLocation(Context context) {
        if (!AllweightsUtils.isRequiredPermisionLocation()) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Arrays.stream(AllweightsUtils.Permission.LOCATION())
                    .anyMatch(permission -> checkPermissionsIfNecessary(permission, context));
        } else {
            for (String permission : AllweightsUtils.Permission.LOCATION()) {
                if (checkPermissionsIfNecessary(permission, context)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean checkPermissionsIfNecessary(@NonNull String permissionSend, Context context) {
        switch (permissionSend) {
            case "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION" -> {
                if (!AllweightsUtils.isRequiredPermisionLocation()) return false;
            }
            case "android.permission.BLUETOOTH_SCAN", "android.permission.BLUETOOTH_CONNECT" -> {
                if (AllweightsUtils.isNotRequiredPermisionBluetooth()) return false;
            }
        }

        return (ContextCompat.checkSelfPermission(context, permissionSend) != PackageManager.PERMISSION_GRANTED);
    }

    @NonNull
    public static Boolean isLocationEnabled(@NonNull Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return LocationManagerCompat.isLocationEnabled(locationManager);
    }

    public static Boolean isBluethoothEnabled() {

        boolean bluetooth_enabled = false;

        try {
            bluetooth_enabled = BluetoothAdapter.getDefaultAdapter().isEnabled();
        } catch (Exception e) {
            Log.e(TAG, "isBluethoothEnabled error: ", e);
        }
        return bluetooth_enabled;
    }
}
