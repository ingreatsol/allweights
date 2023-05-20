package com.ingreatsol.allweights.common;

import android.os.Build;

import androidx.annotation.ChecksSdkIntAtLeast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;

public class AllweightsUtils {

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

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
    public static boolean isRequiredPermisionBluetooth() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }
}
