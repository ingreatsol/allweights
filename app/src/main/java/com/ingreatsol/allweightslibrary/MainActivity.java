package com.ingreatsol.allweightslibrary;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.Settings;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.ingreatsol.allweights.AllweightsUtils;
import com.ingreatsol.allweights.EscanearBluetoothViewModel;
import com.ingreatsol.allweights.exceptions.AllweightsException;
import com.ingreatsol.allweightslibrary.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private EscanearBluetoothViewModel escanearBluetoothViewModel;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<String[]> multiplePermissionLauncher;
    private ActivityResultLauncher<Intent> openAppSettingsLauncher;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        escanearBluetoothViewModel = new ViewModelProvider(this).get(EscanearBluetoothViewModel.class);

        initLauchers();

        ckeckPermissionEscanearBluetooth(this);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @SuppressLint("MissingPermission")
    public void initLauchers() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        try {
                            escanearBluetoothViewModel.escanear(this);
                        } catch (AllweightsException e) {
                            e.printStackTrace();
                        }
                    } else {
                        manejarDenegacionDePermiso();
                        Toast.makeText(this, "Permisos denegados", Toast.LENGTH_LONG).show();
                    }
                });

        multiplePermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean resultStatus = result.entrySet().stream().allMatch(Map.Entry::getValue);
            if (resultStatus) {
                try {
                    escanearBluetoothViewModel.escanear(this);
                } catch (AllweightsException e) {
                    e.printStackTrace();
                }
            } else {
                manejarDenegacionDePermiso();
                Toast.makeText(this, "Permisos denegados", Toast.LENGTH_LONG).show();
            }
        });

        openAppSettingsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    try {
                        escanearBluetoothViewModel.escanear(this);
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
        }
    }

    private void manejarDenegacionDePermiso() {
        ArrayList<String> permissionsShould = AllweightsUtils
                .shouldMapPermission(this, AllweightsUtils.getPermissionEscanearBluetooth().toArray(new String[0]));
        if (permissionsShould.size() > 0) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Estas seguro?")
                    .setMessage("Allweights no puede funcionar correctamente si deniegas este permiso")
                    .setNegativeButton("Si, denegar", (dialogCancel, which) -> dialogCancel.dismiss())
                    .setPositiveButton("Intentar de nuevo", (dialogAcept, which) -> {
                        selectTipeLauncherPermission(AllweightsUtils.getPermissionEscanearBluetooth().toArray(new String[0]));
                        dialogAcept.dismiss();
                    })
                    .show();
        } else {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Configuración de la aplicación?")
                    .setMessage("El permiso solicitado ha sido denegado, para activar este permiso debe ir a la configuración de la aplicación")
                    .setNegativeButton("Entrar sin permiso", (dialogCancel, which) -> {
                        dialogCancel.dismiss();
                    })
                    .setPositiveButton("Ir a configuración", (dialogAcept, which) -> openAppSettingsLauncher.launch(ajustesAplicacion(this)))
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