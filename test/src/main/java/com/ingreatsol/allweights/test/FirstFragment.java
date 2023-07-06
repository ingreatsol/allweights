package com.ingreatsol.allweights.test;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ingreatsol.allweights.common.AllweightsBase;
import com.ingreatsol.allweights.scan.AllweightsBleScan;
import com.ingreatsol.allweights.scan.AllweightsBluetoothScan;
import com.ingreatsol.allweights.scan.AllweightsScan;
import com.ingreatsol.allweights.scan.AllweightsScanCallback;
import com.ingreatsol.allweights.common.AllweightsUtils;
import com.ingreatsol.allweights.common.AllweightsException;
import com.ingreatsol.allweights.test.databinding.FragmentFirstBinding;

import java.util.ArrayList;
import java.util.Map;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private AllweightsScan bluetoothScan;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<String[]> multiplePermissionLauncher;
    private ActivityResultLauncher<Intent> openAppSettingsLauncher;
    private ActivityResultLauncher<Intent> enable_ble_loc_launcher;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private Boolean permisoDenegado = false;

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        bluetoothScan = new AllweightsBluetoothScan(requireActivity());

        mLeDeviceListAdapter = new LeDeviceListAdapter();

        initLauchers();

        return binding.getRoot();
    }

    @SuppressLint("MissingPermission")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.dispositivos.setOnItemClickListener((parent, _view, position, id) -> {
            try {
                final BluetoothDevice device = mLeDeviceListAdapter.getDevice(position);

                if (device == null) return;

                bluetoothScan.stopScan();

                Bundle b = new Bundle();
                b.putString("deviceAddress", device.getAddress());
                b.putInt("deviceType", device.getType());

                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment, b);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        binding.dispositivos.setAdapter(mLeDeviceListAdapter);

        binding.button.setOnClickListener(l -> scanear());
        bluetoothScan.addOnAllweightsScanCallback(new AllweightsScanCallback() {
            @Override
            public void onFoundBluetoothDevice(BluetoothDevice device) {
                mLeDeviceListAdapter.addDevice(device);
            }

            @Override
            public void onAllweightsScanStatusChange(Boolean status) {
                binding.progressBar.setVisibility(status ? View.VISIBLE : View.GONE);
                binding.button.setVisibility(status ? View.GONE : View.VISIBLE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        scanear();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onPause() {
        super.onPause();
        bluetoothScan.stopScan();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bluetoothScan.destroy();
        binding = null;
    }

    @SuppressLint("MissingPermission")
    public void initLauchers() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (!isGranted) {
                        permisoDenegado = true;
                    }
                });

        multiplePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean resultStatus = false;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                resultStatus = result.entrySet().stream().allMatch(Map.Entry::getValue);
            } else {
                for (Map.Entry<String, Boolean> status : result.entrySet()) {
                    resultStatus = status.getValue();
                    if (!resultStatus) {
                        break;
                    }
                }
            }
            if (!resultStatus) {
                permisoDenegado = true;
            }
        });

        enable_ble_loc_launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> Log.d("Test", result.toString()));

        openAppSettingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> Log.d("Test", result.toString()));
    }

    public void launchEnableBle() {
        Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enable_ble_loc_launcher.launch(enableBT);
    }

    public void launchEnableLocation() {
        Intent enableBT = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        enable_ble_loc_launcher.launch(enableBT);
    }

    private void selectTipeLauncherPermission(@NonNull String... permissions) {
        permisoDenegado = false;
        if (permissions.length == 1) {
            requestPermissionLauncher.launch(permissions[0]);
        } else if (permissions.length > 1) {
            multiplePermissionLauncher.launch(permissions);
        }
    }

    @SuppressLint("MissingPermission")
    public void scanear() {
        Context context = getContext();
        assert context != null;
        if (bluetoothScan.isNotExistBluetoothInSystem()) {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Bluetooth no existente")
                    .setMessage("No existe tecnologia bluetooth en este telefono para poder realizar busquedas bluetooth")
                    .setPositiveButton("Aceptar", (dialogAcept, which) -> {
                        dialogAcept.dismiss();
                    })
                    .show();
        } else if (!bluetoothScan.isBluethoothEnabled()) {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Bluetooth desactivado")
                    .setMessage("Se necesita activar el bluetooth para poder detectar dispositivos bluetooth. ¿Desea activarlo?")
                    .setNeutralButton("Cancelar", (dialogCancel, which) -> dialogCancel.dismiss())
                    .setPositiveButton("Activar", (dialogAcept, which) -> {
                        dialogAcept.dismiss();
                        launchEnableBle();
                    })
                    .show();
        } else if (!bluetoothScan.isLocationEnabled()) {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Ubicacion desactivada")
                    .setMessage("Se necesita activar la ubicación para poder detectar dispositivos bluetooth. ¿Desea activarla?")
                    .setNeutralButton("Cancelar", (dialogCancel, which) -> dialogCancel.dismiss())
                    .setPositiveButton("Activar", (dialogAcept, which) -> {
                        dialogAcept.dismiss();
                        launchEnableLocation();
                    })
                    .show();
        } else if (bluetoothScan.isMissingPermisionBluetooth()) {
            if (permisoDenegado) {
                manejarDenegacionDePermiso(AllweightsUtils.Permission.BLUETOOTH);
            } else {
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Permiso de buetooth")
                        .setMessage("Necesita el permiso de escanear bluetooth para detectar la balanza. ¿Desea otorgarlo?")
                        .setNeutralButton("Cancelar", (dialogCancel, which) -> dialogCancel.dismiss())
                        .setPositiveButton("Activar permiso", (dialogAcept, which) -> {
                            dialogAcept.dismiss();
                            selectTipeLauncherPermission(AllweightsUtils.Permission.BLUETOOTH);
                        })
                        .show();
            }
        } else if (bluetoothScan.isMissingPermisionLocation()) {
            if (permisoDenegado) {
                manejarDenegacionDePermiso(AllweightsUtils.Permission.LOCATION());
            } else {
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Permiso de ubicación")
                        .setMessage("Necesita el permiso de ubicación para detectar la balanza. ¿Desea otorgarlo?")
                        .setNeutralButton("Cancelar", (dialogCancel, which) -> dialogCancel.dismiss())
                        .setPositiveButton("Activar permiso", (dialogAcept, which) -> {
                            dialogAcept.dismiss();
                            selectTipeLauncherPermission(AllweightsUtils.Permission.LOCATION());
                        })
                        .show();
            }
        } else {
            try {
                mLeDeviceListAdapter.clear();
                bluetoothScan.scan();
            } catch (AllweightsException e) {
                Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void manejarDenegacionDePermiso(@NonNull String... permissionSend) {
        ArrayList<String> permissionsShould = AllweightsUtils.shouldMapPermission(requireActivity(), permissionSend);
        if (permissionsShould.size() > 0) {
            new MaterialAlertDialogBuilder(requireActivity())
                    .setTitle("Estas seguro?")
                    .setMessage("Allweights no puede funcionar correctamente si deniegas este permiso")
                    .setNegativeButton("Si, denegar", (dialogCancel, which) -> dialogCancel.dismiss())
                    .setPositiveButton("Intentar de nuevo", (dialogAcept, which) -> {
                        selectTipeLauncherPermission(permissionsShould.toArray(new String[0]));
                        dialogAcept.dismiss();
                    })
                    .show();
        } else {
            new MaterialAlertDialogBuilder(requireActivity())
                    .setTitle("Configuración de la aplicación?")
                    .setMessage("El permiso solicitado ha sido denegado, para activar este permiso debe ir a la configuración de la aplicación")
                    .setNegativeButton("Entrar sin permiso", (dialogCancel, which) -> dialogCancel.dismiss())
                    .setPositiveButton("Ir a configuración", (dialogAcept, which) -> {
                        permisoDenegado = false;
                        openAppSettingsLauncher.launch(ajustesAplicacion(requireActivity()));
                        dialogAcept.dismiss();
                    })
                    .show();
        }
    }

    @NonNull
    public Intent ajustesAplicacion(@NonNull Context fragmentActivity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + fragmentActivity.getPackageName()));
        return intent;
    }
}