package com.ingreatsol.allweightslibrary;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ingreatsol.allweights.AllweightsUtils;
import com.ingreatsol.allweights.AllweightsScan;
import com.ingreatsol.allweights.exceptions.AllweightsException;
import com.ingreatsol.allweightslibrary.databinding.FragmentFirstBinding;

import java.util.ArrayList;
import java.util.Map;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private AllweightsScan bluetoothScan;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<String[]> multiplePermissionLauncher;
    private ActivityResultLauncher<Intent> openAppSettingsLauncher;

    Observer<Boolean> estadoCOnexionObserve = new Observer<Boolean>() {
        @Override
        public void onChanged(Boolean estado) {
            binding.progressBar.setVisibility(estado ? View.VISIBLE : View.GONE);
            binding.button.setVisibility(estado ? View.GONE : View.VISIBLE);
        }
    };

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        bluetoothScan = new AllweightsScan();

        initLauchers();

        bluetoothScan.init(this,
                R.layout.listitem_device,
                R.id.device_address,
                R.id.device_name);

        return binding.getRoot();
    }

    @SuppressLint("MissingPermission")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.dispositivos.setOnItemClickListener((parent, _view, position, id) -> {
            try {
                final BluetoothDevice device = bluetoothScan.getDevice(position);

                if (device == null) return;

                bluetoothScan.cancelDiscovery();

                Bundle b = new Bundle();
                b.putParcelable("device", device);

                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment, b);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        binding.dispositivos.setAdapter(bluetoothScan.getmLeDeviceListAdapter());

        binding.button.setOnClickListener(l -> ckeckPermissionEscanearBluetooth(requireActivity()));
    }

    @Override
    public void onResume() {
        super.onResume();
        bluetoothScan.getScanState().observe(requireActivity(), estadoCOnexionObserve);
        ckeckPermissionEscanearBluetooth(requireActivity());
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onPause() {
        super.onPause();
        bluetoothScan.stopScan();
        bluetoothScan.getScanState().removeObserver(estadoCOnexionObserve);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @SuppressLint("MissingPermission")
    public void initLauchers() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        try {
                            bluetoothScan.scan(requireActivity());
                        } catch (AllweightsException e) {
                            e.printStackTrace();
                        }
                    } else {
                        manejarDenegacionDePermiso();
                        Toast.makeText(requireActivity(), "Permisos denegados", Toast.LENGTH_LONG).show();
                    }
                });

        multiplePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean resultStatus = result.entrySet().stream().allMatch(Map.Entry::getValue);
            if (resultStatus) {
                try {
                    bluetoothScan.scan(requireActivity());
                } catch (AllweightsException e) {
                    e.printStackTrace();
                }
            } else {
                manejarDenegacionDePermiso();
                Toast.makeText(requireActivity(), "Permisos denegados", Toast.LENGTH_LONG).show();
            }
        });

        openAppSettingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    try {
                        bluetoothScan.scan(requireActivity());
                    } catch (AllweightsException e) {
                        e.printStackTrace();
                    }
                });

    }

    private void selectTipeLauncherPermission(@NonNull String... permissions) {
        if (permissions.length == 1) {
            requestPermissionLauncher.launch(permissions[0]);
        } else if (permissions.length > 1) {
            multiplePermissionLauncher.launch(permissions);
        }
    }

    @SuppressLint("MissingPermission")
    public void ckeckPermissionEscanearBluetooth(FragmentActivity fragmentActivity) {
        String message = "Necesita el permiso de escanear bluetooth para detectar la balanza. ¿Desea otorgarlo?";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            message = "Necesita el permiso de escanear bluetooth y de ubicación para detectar la balanza. ¿Desea otorgarlos?";
        }
        if (AllweightsUtils.isMissingPermisionsEscanearBluetooth(fragmentActivity.getApplication())) {
            new MaterialAlertDialogBuilder(fragmentActivity)
                    .setTitle("Permiso denegado")
                    .setMessage(message)
                    .setNeutralButton("Cancelar", (dialogCancel, which) -> dialogCancel.dismiss())
                    .setPositiveButton("Activar permiso", (dialogAcept, which) ->
                            selectTipeLauncherPermission(AllweightsUtils.getPermissionEscanearBluetooth().toArray(new String[0])))
                    .show();
        } else {
            try {
                bluetoothScan.scan(requireActivity());
            } catch (AllweightsException e) {
                Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void manejarDenegacionDePermiso() {
        ArrayList<String> permissionsShould = AllweightsUtils
                .shouldMapPermission(requireActivity(), AllweightsUtils.getPermissionEscanearBluetooth().toArray(new String[0]));
        if (permissionsShould.size() > 0) {
            new MaterialAlertDialogBuilder(requireActivity())
                    .setTitle("Estas seguro?")
                    .setMessage("Allweights no puede funcionar correctamente si deniegas este permiso")
                    .setNegativeButton("Si, denegar", (dialogCancel, which) -> dialogCancel.dismiss())
                    .setPositiveButton("Intentar de nuevo", (dialogAcept, which) -> {
                        selectTipeLauncherPermission(AllweightsUtils.getPermissionEscanearBluetooth().toArray(new String[0]));
                        dialogAcept.dismiss();
                    })
                    .show();
        } else {
            new MaterialAlertDialogBuilder(requireActivity())
                    .setTitle("Configuración de la aplicación?")
                    .setMessage("El permiso solicitado ha sido denegado, para activar este permiso debe ir a la configuración de la aplicación")
                    .setNegativeButton("Entrar sin permiso", (dialogCancel, which) -> dialogCancel.dismiss())
                    .setPositiveButton("Ir a configuración", (dialogAcept, which) -> openAppSettingsLauncher.launch(ajustesAplicacion(requireActivity())))
                    .show();
        }
    }

    @NonNull
    public Intent ajustesAplicacion(@NonNull FragmentActivity fragmentActivity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + fragmentActivity.getPackageName()));
        return intent;
    }
}