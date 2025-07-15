package com.example.proyectounidadii.ui.listado;

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
import com.example.proyectounidadii.databinding.FragmentListadoBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ListadoFragment extends Fragment {
    private FragmentListadoBinding binding;
    private BluetoothAdapter btAdapter;     // Adaptador Bluetooth local

    public ListadoFragment() { /* Constructor vacío requerido */ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentListadoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @RequiresPermission(allOf = {
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
    })
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btAdapter = BluetoothAdapter.getDefaultAdapter(); // Instancia del adaptador
        MainActivity main = (MainActivity) requireActivity(); // Referencia a la actividad principal

        // Observamos el LiveData de estado para actualizar el TextView en tiempo real
        main.getEstado().observe(getViewLifecycleOwner(),
                estado -> binding.txtEstado.setText("Estado: " + estado));

        // Al pulsar el botón, mostramos diálogo con dispositivos emparejados
        binding.btnSelect.setOnClickListener(v -> mostrarEmparejados(main));
    }

    /**
     * Muestra en un AlertDialog la lista de dispositivos emparejados.
     * Al seleccionar uno, se llama a main.conectar() para iniciar la conexión.
     */
    @RequiresPermission(allOf = {
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
    })
    private void mostrarEmparejados(MainActivity main) {
        // Validamos que Bluetooth esté disponible y activado
        if (btAdapter == null || !btAdapter.isEnabled()) {
            toast("Activa Bluetooth");
            return;
        }

        // Obtenemos el conjunto de dispositivos emparejados
        Set<BluetoothDevice> paired = btAdapter.getBondedDevices();
        if (paired.isEmpty()) {
            toast("Sin dispositivos emparejados");
            return;
        }

        // Convertimos el Set en List para indexarla fácilmente
        List<BluetoothDevice> devs = new ArrayList<>(paired);
        String[] items = new String[devs.size()];
        for (int i = 0; i < devs.size(); i++) {
            // Mostramos nombre y dirección MAC en cada elemento del diálogo
            items[i] = devs.get(i).getName() + "\n" + devs.get(i).getAddress();
        }

        // Construimos y mostramos el AlertDialog
        new AlertDialog.Builder(getContext())
                .setTitle("Selecciona dispositivo")
                .setItems(items, (d, idx) -> {
                    // Cuando el usuario selecciona un dispositivo, delegamos la conexión a MainActivity
                    main.conectar(devs.get(idx));
                })
                .show();
    }

    /**
     * Utilidad para mostrar Toasts de forma concisa.
     */
    private void toast(String s) {
        Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
    }

    /**
     * Al destruir la vista, anulamos la referencia del binding para evitar fugas de memoria.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}