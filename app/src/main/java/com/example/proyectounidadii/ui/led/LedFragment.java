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
import com.example.proyectounidadii.databinding.FragmentLedBinding;

public class LedFragment extends Fragment {
    private FragmentLedBinding binding; // ViewBinding para acceder a las vistas

    public LedFragment() {
        // Constructor vacío requerido por el sistema
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflamos el layout y obtenemos el objeto binding
        binding = FragmentLedBinding.inflate(inflater, container, false);
        return binding.getRoot(); // Retornamos la raíz del layout
    }

    /**
     * Método llamado cuando la vista ya fue creada.
     * Se configuran los observadores y eventos de los botones.
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Obtenemos una referencia a la actividad principal para acceder al socket y a los LiveData
        MainActivity main = (MainActivity) requireActivity();

        // Observamos el estado de la conexión Bluetooth
        main.getEstado().observe(getViewLifecycleOwner(),
                estado -> binding.txtEstado.setText("Estado: " + estado));

        // Observamos el estado actual del LED (encendido o apagado)
        main.getLed().observe(getViewLifecycleOwner(), d -> {
            if (d != null) {
                String estadoLed = d.encendido ? "Encendido" : "Apagado";
                binding.txtLed.setText("Led: " + estadoLed);
            }
        });

        // Botón para ENCENDER el LED: envía el carácter '1' por Bluetooth
        binding.btnOn.setOnClickListener(v -> {
            if (main.getSocket() != null && main.getSocket().isConnected()) {
                try {
                    main.getSocket().getOutputStream().write('1'); // Comando para encender el LED
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Botón para APAGAR el LED: envía el carácter '0' por Bluetooth
        binding.btnOff.setOnClickListener(v -> {
            if (main.getSocket() != null && main.getSocket().isConnected()) {
                try {
                    main.getSocket().getOutputStream().write('0'); // Comando para apagar el LED
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
