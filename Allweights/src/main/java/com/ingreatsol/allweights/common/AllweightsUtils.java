package com.ingreatsol.allweights.common;

import android.os.Build;

import androidx.annotation.ChecksSdkIntAtLeast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;

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
}
