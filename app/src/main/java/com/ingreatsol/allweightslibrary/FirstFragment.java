package com.ingreatsol.allweightslibrary;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.ingreatsol.allweights.AllweightsUtils;
import com.ingreatsol.allweights.EscanearBluetoothViewModel;
import com.ingreatsol.allweights.exceptions.AllweightsException;
import com.ingreatsol.allweightslibrary.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private EscanearBluetoothViewModel escanearBluetoothViewModel;

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        escanearBluetoothViewModel = new ViewModelProvider(requireActivity()).get(EscanearBluetoothViewModel.class);

        escanearBluetoothViewModel.init(requireActivity(),
                R.layout.listitem_device,
                R.id.device_address,
                R.id.device_name);

        try {
            if (!AllweightsUtils.isMissingPermisionsEscanearBluetooth(requireActivity().getApplication())) {
                escanearBluetoothViewModel.escanear(requireActivity());
            }
        } catch (AllweightsException e) {
            Toast.makeText(requireActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }

        binding.dispositivos.setOnItemClickListener((parent, view, position, id) -> {
            try {
                final BluetoothDevice device = escanearBluetoothViewModel.getDevice(position);
                if (device == null) return;
                escanearBluetoothViewModel.cancelDiscovery();
                Toast.makeText(requireActivity(), "Mac: " + device.getAddress() + ", Nombre: " + device.getName(), Toast.LENGTH_LONG).show();
//                App.getInstance().getPreferences().save(App.Config.BALANZA_ADDRESS_KEY, device.getAddress());
//                App.getInstance().getPreferences().save(App.Config.BALANZA_NAME_KEY, device.getName());
//
//                ContextUtil.setCurrentFragment(this.requireActivity(), R.id.content_frame, PesajeFragment.class, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        binding.dispositivos.setAdapter(escanearBluetoothViewModel.getmLeDeviceListAdapter());

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


//        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
//            }
//        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}