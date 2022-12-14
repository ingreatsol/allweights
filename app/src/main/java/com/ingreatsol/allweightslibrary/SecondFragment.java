package com.ingreatsol.allweightslibrary;

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
import androidx.lifecycle.Observer;

import com.ingreatsol.allweights.AllweightsConnect;
import com.ingreatsol.allweights.AllweightsData;
import com.ingreatsol.allweights.AllweightsUtils;
import com.ingreatsol.allweights.ConnectionStatus;
import com.ingreatsol.allweightslibrary.databinding.FragmentSecondBinding;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private AllweightsConnect allweightsConnect;

    private final Observer<AllweightsData> dataObserver = new Observer<AllweightsData>() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onChanged(@NonNull AllweightsData allweightsData) {
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
            if (allweightsData.bateryPercent != null){
                binding.progressbar.setProgress((int) (((allweightsData.bateryPercent - AllweightsUtils.RANGO_MINIMO_BATERIA) / AllweightsUtils.LIMITE_BATERIA) * 100));
            }
        }
    };
    private final Observer<ConnectionStatus> estadoConexionObserve = new Observer<ConnectionStatus>() {
        @Override
        public void onChanged(@NonNull ConnectionStatus estado) {
            binding.textViewEstado.setText(estado.toString());
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentSecondBinding.inflate(inflater, container, false);

        allweightsConnect = new AllweightsConnect();

        allweightsConnect.init(requireActivity(), getArguments().getParcelable("device"));

        return binding.getRoot();

    }

    @SuppressLint("MissingPermission")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonSecond.setOnClickListener(l -> {
            if (!allweightsConnect.waxScale()){
                Toast.makeText(requireActivity(), "Error al encerar", Toast.LENGTH_LONG).show();
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
        allweightsConnect.getData().observe(requireActivity(), dataObserver);
        allweightsConnect.getConnectionStatus().observe(requireActivity(), estadoConexionObserve);
        allweightsConnect.registerService(requireActivity());
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onPause() {
        super.onPause();
        allweightsConnect.getData().removeObserver(dataObserver);
        allweightsConnect.getConnectionStatus().removeObserver(estadoConexionObserve);
        allweightsConnect.unRegisterService(requireActivity());
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        allweightsConnect.destroyService(requireActivity());
    }

}