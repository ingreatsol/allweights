package com.ingreatsol.allweights;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Arrays;

public class AllweightsUtils {

    public static final class Permission {
        public static final String[] LOCATION = new String[]{
                "android.permission.ACCESS_FINE_LOCATION",
                "android.permission.ACCESS_COARSE_LOCATION"};
        public static final String[] BLUETOOTH = new String[]{
                "android.permission.BLUETOOTH_SCAN",
                "android.permission.BLUETOOTH_CONNECT"};
    }

    public static boolean checkPermissionsIfNecessary(Application application, @NonNull String permissionSend) {
        switch (permissionSend) {
            case "android.permission.ACCESS_FINE_LOCATION":
            case "android.permission.ACCESS_COARSE_LOCATION":
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) return false;
            case "android.permission.BLUETOOTH_SCAN":
            case "android.Permission.BLUETOOTH_ADVERTISE":
            case "android.permission.BLUETOOTH_CONNECT":
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false;
        }
        boolean result = (ActivityCompat.checkSelfPermission(application, permissionSend) != PackageManager.PERMISSION_GRANTED);
        return result;
    }

    @NonNull
    public static ArrayList<String> getPermissionEscanearBluetooth(){
        ArrayList<String> list = new ArrayList<>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            list.addAll(Arrays.asList(Permission.LOCATION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            list.addAll(Arrays.asList(Permission.BLUETOOTH));
        }
        return list;
    }

    public static boolean isMissingPermisionsEscanearBluetooth(Application application) {
        return !getPermissionEscanearBluetooth().stream().allMatch(permissionSend -> !checkPermissionsIfNecessary(application, permissionSend));
    }

    @NonNull
    public static ArrayList<String> shouldMapPermission(Activity activity, @NonNull String... permissionSend) {
        ArrayList<String> permissionsShoulShow = new ArrayList<>();
        for (String permission : permissionSend) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                permissionsShoulShow.add(permission);
            }
        }
        return permissionsShoulShow;
    }
}
