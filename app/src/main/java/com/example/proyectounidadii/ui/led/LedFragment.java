package com.example.proyectounidadii.ui.led;

import android.Manifest;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.proyectounidadii.MainActivity;
import com.example.proyectounidadii.R;
import com.example.proyectounidadii.databinding.FragmentLedBinding;

public class LedFragment extends Fragment {
    private FragmentLedBinding binding;

    public LedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentLedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity main = (MainActivity) requireActivity();

        // Observa estado
        main.getEstado().observe(getViewLifecycleOwner(),
                estado -> binding.txtEstado.setText("Estado: " + estado));

        main.getLed().observe(getViewLifecycleOwner(), d -> {
            if (d != null) {
                String estadoLed = d.encendido ? "Encendido" : "Apagado";
                binding.txtLed.setText("Led: " + estadoLed);
            }
        });

        // Boton Encender
        binding.btnOn.setOnClickListener(v -> {
            if(main.getSocket() != null && main.getSocket().isConnected()){
                try {
                    main.getSocket().getOutputStream().write('1');
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Boton Apagar
        binding.btnOff.setOnClickListener(v -> {
            if(main.getSocket() != null && main.getSocket().isConnected()){
                try {
                    main.getSocket().getOutputStream().write('0');
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}