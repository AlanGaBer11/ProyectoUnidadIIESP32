package com.example.proyectounidadii.ui.dht;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.proyectounidadii.R;
import com.example.proyectounidadii.databinding.FragmentDht11Binding;
import com.example.proyectounidadii.models.SharedBluetoothViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Dht11Fragment extends Fragment {
    private FragmentDht11Binding binding;
    private SharedBluetoothViewModel vm;
    private BluetoothAdapter btAdapter;


    public Dht11Fragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDht11Binding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vm = new ViewModelProvider(requireActivity()).get(SharedBluetoothViewModel.class);
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        vm.getEstado().observe(getViewLifecycleOwner(),
                estado -> binding.txtEstado.setText("Estado: " + estado));

        vm.getDht().observe(getViewLifecycleOwner(), d -> {
            if (d != null) {
                binding.txtHum.setText("Humedad: " + d.humedad + " %");
                binding.txtTemp.setText("Temperatura: " + d.temperatura + " °C");
            }
        });

        binding.btnSelect.setOnClickListener(v -> mostrarEmparejados());
    }

    /* --- METODO PARA MOSTRAR LOS DISPOSITIVOS BLUETOOTH --- */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    private void mostrarEmparejados() {
        if (btAdapter == null || !btAdapter.isEnabled()) {
            toast("Activa Bluetooth");
            return;
        }
        Set<BluetoothDevice> paired = btAdapter.getBondedDevices();
        if (paired.isEmpty()) {
            toast("Sin emparejados");
            return;
        }

        List<BluetoothDevice> devs = new ArrayList<>(paired);
        String[] items = new String[devs.size()];
        for (int i = 0; i < devs.size(); i++)
            items[i] = devs.get(i).getName() + "\n" + devs.get(i).getAddress();

        new AlertDialog.Builder(getContext())
                .setTitle("Selecciona dispositivo")
                .setItems(items, (d, idx) -> vm.conectar(devs.get(idx)))
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