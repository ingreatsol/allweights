package com.ingreatsol.allweights;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

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
}
