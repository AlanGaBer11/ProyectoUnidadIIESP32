package com.example.proyectounidadii.ui.dht;

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
import com.example.proyectounidadii.databinding.FragmentDht11Binding;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Dht11Fragment extends Fragment {
    private FragmentDht11Binding binding;
    private BluetoothAdapter btAdapter;

    public Dht11Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDht11Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresPermission(allOf = {
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
    })
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        MainActivity main = (MainActivity) requireActivity();

        // Observa estado
        main.getEstado().observe(getViewLifecycleOwner(),
                estado -> binding.txtEstado.setText("Estado: " + estado));

        // Observa datos DHT
        main.getDht().observe(getViewLifecycleOwner(), d -> {
            if (d != null) {
                binding.txtHum.setText("Humedad: " + d.humedad + " %");
                binding.txtTemp.setText("Temperatura: " + d.temperatura + " °C");
            }
        });

        binding.btnSelect.setOnClickListener(v -> mostrarEmparejados(main));
    }

    @RequiresPermission(allOf = {
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
    })
    private void mostrarEmparejados(MainActivity main) {
        if (btAdapter == null || !btAdapter.isEnabled()) {
            toast("Activa Bluetooth");
            return;
        }

        Set<BluetoothDevice> paired = btAdapter.getBondedDevices();
        if (paired.isEmpty()) {
            toast("Sin dispositivos emparejados");
            return;
        }

        List<BluetoothDevice> devs = new ArrayList<>(paired);
        String[] items = new String[devs.size()];
        for (int i = 0; i < devs.size(); i++) {
            items[i] = devs.get(i).getName() + "\n" + devs.get(i).getAddress();
        }

        new AlertDialog.Builder(getContext())
                .setTitle("Selecciona dispositivo")
                .setItems(items, (d, idx) -> {
                    // 🔁 usamos el método conectar() de MainActivity
                    main.conectar(devs.get(idx));
                })
                .show();
    }

    private void toast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
