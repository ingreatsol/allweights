package com.ingreatsol.allweightslibrary;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.fragment.NavHostFragment;

import com.ingreatsol.allweights.AllweightsConnect;
import com.ingreatsol.allweights.AllweightsData;
import com.ingreatsol.allweights.EstadoConexion;
import com.ingreatsol.allweights.exceptions.AllweightsException;
import com.ingreatsol.allweightslibrary.databinding.FragmentSecondBinding;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private AllweightsConnect allweightsConnect;

    private final Observer<AllweightsData> dataObserver = new Observer<AllweightsData>() {
        @SuppressLint("SetTextI18n")
        @Override
        public void onChanged(@NonNull AllweightsData allweightsData) {
            binding.textviewPeso.setText(allweightsData.peso.toString());
        }
    };
    private final Observer<EstadoConexion> estadoConexionObserve = new Observer<EstadoConexion>() {
        @Override
        public void onChanged(@NonNull EstadoConexion estado) {
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
            try {
                allweightsConnect.encerar_balanza();
            } catch (AllweightsException e) {
                e.printStackTrace();
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        super.onResume();
        allweightsConnect.getData().observe(requireActivity(), dataObserver);
        allweightsConnect.getIsConnected().observe(requireActivity(), estadoConexionObserve);
        allweightsConnect.registar_sevicio(requireActivity());
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onPause() {
        super.onPause();
        allweightsConnect.getData().removeObserver(dataObserver);
        allweightsConnect.getIsConnected().removeObserver(estadoConexionObserve);
        allweightsConnect.quitar_servicio(requireActivity());
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        allweightsConnect.eliminar_servicio(requireActivity());
    }

}