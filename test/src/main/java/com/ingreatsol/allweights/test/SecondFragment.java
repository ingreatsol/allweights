package com.ingreatsol.allweights.test;

import static com.ingreatsol.allweights.common.AllweightsUtils.LIMITE_BATERIA;
import static com.ingreatsol.allweights.common.AllweightsUtils.RANGO_MINIMO_BATERIA;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ingreatsol.allweights.connect.AllweightsBleConnect;
import com.ingreatsol.allweights.connect.AllweightsBluetoothConnect;
import com.ingreatsol.allweights.connect.AllweightsConnect;
import com.ingreatsol.allweights.test.databinding.FragmentSecondBinding;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private AllweightsConnect allweightsConnect;

    private final AllweightsConnect.DataChangeListener dataChangeListener = data -> {
        binding.textviewPeso.setText(data.weight.toString());
        if (Boolean.TRUE.equals(data.isEnergyConnected)) {
            binding.progressbar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
        } else {
            if (data.bateryPercent != null && RANGO_MINIMO_BATERIA < data.bateryPercent) {
                binding.progressbar.setProgressTintList(ColorStateList.valueOf(Color.RED));
            } else {
                binding.progressbar.setProgressTintList(ColorStateList.valueOf(Color.BLUE));
            }
        }
        if (data.bateryPercent != null) {
            binding.progressbar.setProgress((int) (((data.bateryPercent - RANGO_MINIMO_BATERIA) / LIMITE_BATERIA) * 100));
        }
    };

    private final AllweightsConnect.ConnectionStatusChangeListener statusChangeListener =
            status -> binding.textViewEstado.setText(status.toString());

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);

        assert getArguments() != null;

        String deviceAddres = getArguments().getString("deviceAddress");
        int deviceType = getArguments().getInt("deviceType");

        if (deviceType == 1) {
            allweightsConnect = new AllweightsBluetoothConnect(requireActivity());
        } else {
            allweightsConnect = new AllweightsBleConnect(requireActivity());
        }

        allweightsConnect.setDevice(deviceAddres, deviceType);
        return binding.getRoot();
    }

    @SuppressLint({"MissingPermission", "SetTextI18n"})
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonEncerar.setOnClickListener(l -> {
            if (!allweightsConnect.waxScale()) {
                Toast.makeText(requireActivity(), "Error al encerar", Toast.LENGTH_LONG).show();
            }
        });
        binding.buttonCalibrar1.setOnClickListener(l -> {
            if (!allweightsConnect.calibrateScale(1)) {
                Toast.makeText(requireActivity(), "Error al calibrar", Toast.LENGTH_LONG).show();
            }
        });
        binding.buttonVelocidad2.setOnClickListener(l -> {
            if (!allweightsConnect.sampleQuantity(2)) {
                Toast.makeText(requireActivity(), "Error al escribir la velocidad", Toast.LENGTH_LONG).show();
            }
        });
        binding.buttonVelocidad3.setOnClickListener(l -> {
            if (!allweightsConnect.sampleQuantity(3)) {
                Toast.makeText(requireActivity(), "Error al escribir la velocidad", Toast.LENGTH_LONG).show();
            }
        });
        binding.buttonVelocidad4.setOnClickListener(l -> {
            if (!allweightsConnect.sampleQuantity(4)) {
                Toast.makeText(requireActivity(), "Error al escribir la velocidad", Toast.LENGTH_LONG).show();
            }
        });

        binding.buttonConectar.setOnClickListener(l -> connect());
        binding.buttonDesconectar.setOnClickListener(l -> allweightsConnect.disconnect());
    }

    @Override
    public void onResume() {
        super.onResume();
        allweightsConnect.addOnConnectionStatusChangeListener(statusChangeListener);
        allweightsConnect.addOnDataChangeListener(dataChangeListener);
        connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        allweightsConnect.removeOnConnectionStatusChangeListener(statusChangeListener);
        allweightsConnect.removeOnDataChangeListener(dataChangeListener);
        allweightsConnect.disconnect();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        allweightsConnect.destroy();
    }

    @SuppressLint("MissingPermission")
    public void connect() {
        try {
            allweightsConnect.connect();
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}