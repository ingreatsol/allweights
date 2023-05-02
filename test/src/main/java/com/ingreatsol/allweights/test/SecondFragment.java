package com.ingreatsol.allweights.test;

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

import com.ingreatsol.allweights.AllweightsConnect;
import com.ingreatsol.allweights.AllweightsUtils;
import com.ingreatsol.allweights.ConnectionStatus;
import com.ingreatsol.allweights.test.databinding.FragmentSecondBinding;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private AllweightsConnect allweightsConnect;

    AllweightsConnect.OnConnectionStatusListener onConnectionStatusListener = new AllweightsConnect.OnConnectionStatusListener() {
        @Override
        public void onConnectionStatus(@NonNull ConnectionStatus status) {
            binding.textViewEstado.setText(status.toString());
        }
    };

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);

        assert getArguments() != null;

        String deviceAddres = getArguments().getString("deviceAddress");
        Integer deviceType = getArguments().getInt("deviceType");

        allweightsConnect = new AllweightsConnect();
        allweightsConnect.setDevice(deviceAddres, deviceType);

        return binding.getRoot();
    }

    @SuppressLint("MissingPermission")
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

        binding.buttonConectar.setOnClickListener(l -> connect());
        binding.buttonDesconectar.setOnClickListener(l -> allweightsConnect.disconnect());
        allweightsConnect.addOnAllweightsDataListener(allweightsData -> {
            binding.textviewPeso.setText(allweightsData.weight.toString());
            if (Boolean.TRUE.equals(allweightsData.isEnergyConnected)) {
                binding.progressbar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
            } else {
                if (allweightsData.bateryPercent != null && AllweightsUtils.RANGO_MINIMO_BATERIA < allweightsData.bateryPercent) {
                    binding.progressbar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                } else {
                    binding.progressbar.setProgressTintList(ColorStateList.valueOf(Color.BLUE));
                }
            }
            if (allweightsData.bateryPercent != null) {
                binding.progressbar.setProgress((int) (((allweightsData.bateryPercent - AllweightsUtils.RANGO_MINIMO_BATERIA) / AllweightsUtils.LIMITE_BATERIA) * 100));
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        allweightsConnect.registerService(requireActivity());
        allweightsConnect.addOnConnectionStatusListener(onConnectionStatusListener);
        connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        allweightsConnect.unRegisterService(requireActivity());
        allweightsConnect.removeOnConnectionStatusListener(onConnectionStatusListener);
        allweightsConnect.disconnect();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        allweightsConnect.destroyService(requireActivity());
    }

    @SuppressLint("MissingPermission")
    public void connect() {
        try {
            allweightsConnect.connect(requireActivity());
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

}