package com.example.proyectounidadii.ui.sensores;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.proyectounidadii.MainActivity;
import com.example.proyectounidadii.databinding.FragmentSensoresBinding;
import com.example.proyectounidadii.models.SharedBluetoothViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SensoresFragment extends Fragment {
    private FragmentSensoresBinding binding;
    private BluetoothAdapter btAdapter;


    public SensoresFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSensoresBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        MainActivity main = (MainActivity) requireActivity();

        // Observa estado
        main.getEstado().observe(getViewLifecycleOwner(),
                estado -> binding.txtEstado.setText("Estado: " + estado));

        main.getDht().observe(getViewLifecycleOwner(), d -> {
            if (d != null) {
                binding.txtHum.setText("Humedad: " + d.humedad + "%");
                binding.txtTemp.setText("Temperatura: " + d.temperatura + "°C");
            }
        });

        main.getFotosensible().observe(getViewLifecycleOwner(), d -> {
            if (d != null) {
                binding.txtLdr.setText("LDR: " + d.valorRaw);
                binding.txtLuz.setText("Luz: " + d.luz + "%");
            }
        });
    }


    private void toast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
    }
}