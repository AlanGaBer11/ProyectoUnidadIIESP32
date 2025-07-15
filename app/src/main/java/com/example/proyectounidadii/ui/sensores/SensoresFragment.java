package com.example.proyectounidadii.ui.sensores;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
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

public class SensoresFragment extends Fragment {
    private FragmentSensoresBinding binding; // ViewBinding para acceder a las vistas del layout
    private BluetoothAdapter btAdapter; // Adaptador Bluetooth del dispositivo

    public SensoresFragment() {
        // Constructor vacío requerido por el sistema
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Se infla la vista y se guarda el binding
        binding = FragmentSensoresBinding.inflate(inflater, container, false);
        return binding.getRoot(); // Devuelve la raíz de la vista inflada
    }

    /**
     * Se ejecuta después de que la vista ha sido creada.
     * Aquí se configuran los observadores para los sensores y el estado de la conexión.
     */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Se obtiene el adaptador Bluetooth (aunque aquí no se utiliza directamente)
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        // Se accede a la actividad principal para obtener los datos compartidos
        MainActivity main = (MainActivity) requireActivity();

        // Observador del estado de la conexión Bluetooth
        main.getEstado().observe(getViewLifecycleOwner(),
                estado -> binding.txtEstado.setText("Estado: " + estado));

        // Observador de datos del sensor DHT (temperatura y humedad)
        main.getDht().observe(getViewLifecycleOwner(), d -> {
            if (d != null) {
                // Se actualizan los TextView con los valores
                binding.txtHum.setText("Humedad: " + d.humedad + "%");
                binding.txtTemp.setText("Temperatura: " + d.temperatura + "°C");
            }
        });

        // Observador de datos del sensor fotosensible (LDR)
        main.getFotosensible().observe(getViewLifecycleOwner(), d -> {
            if (d != null) {
                // Se actualizan los TextView con los valores de luz y valor bruto del LDR
                binding.txtLdr.setText("LDR: " + d.valorRaw);
                binding.txtLuz.setText("Luz: " + d.luz + "%");
            }
        });
    }

    /**
     * Método auxiliar para mostrar mensajes tipo Toast.
     */
    private void toast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
    }
}
