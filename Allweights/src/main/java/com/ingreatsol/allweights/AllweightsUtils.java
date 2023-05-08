package com.ingreatsol.allweights;

import android.bluetooth.BluetoothAdapter;
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
                break;
            case "android.permission.BLUETOOTH_SCAN":
            case "android.Permission.BLUETOOTH_ADVERTISE":
            case "android.permission.BLUETOOTH_CONNECT":
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false;
                break;
        }

        return (ContextCompat.checkSelfPermission(context, permissionSend) != PackageManager.PERMISSION_GRANTED);
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
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Arrays.stream(Permission.LOCATION)
                    .anyMatch(per -> checkPermissionsIfNecessary(context, per));
        } else {
            for (String permission : Permission.LOCATION) {
                if (checkPermissionsIfNecessary(context, permission)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static boolean isMissingPermisionBluetooth(Context context) {
        if (!isRequiredPermisionBluetooth()) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Arrays.stream(Permission.BLUETOOTH)
                    .anyMatch(per -> checkPermissionsIfNecessary(context, per));
        } else {
            for (String permission : Permission.BLUETOOTH) {
                if (checkPermissionsIfNecessary(context, permission)) {
                    return true;
                }
            }
            return false;
        }
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

    public static Boolean isBluethoothEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        boolean bluetooth_enabled = false;

        try {
            bluetooth_enabled = bluetoothAdapter.isEnabled();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return bluetooth_enabled;
    }

    public static boolean isExistBluetoothInSystem(@NonNull Context context){
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
                || context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
    }
}
