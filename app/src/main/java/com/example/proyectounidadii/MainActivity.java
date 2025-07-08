package com.example.proyectounidadii;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import com.example.proyectounidadii.databinding.ActivityMainBinding;
import com.example.proyectounidadii.ui.dht.Dht11Fragment;
import com.example.proyectounidadii.ui.fotosensible.FotosensibleFragment;
import com.example.proyectounidadii.ui.movimiento.MovimientoFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        pedirPermisosBluetooth();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Dht11Fragment()).commit();

        binding.btnDht11.setOnClickListener(view -> showFragment(new Dht11Fragment()));
        binding.btnPhoto.setOnClickListener(view -> showFragment(new FotosensibleFragment()));
        binding.btnMovement.setOnClickListener(view -> showFragment(new MovimientoFragment()));

    }

    private void pedirPermisosBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            List<String> permisos = new ArrayList<>();

            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permisos.add(Manifest.permission.BLUETOOTH_CONNECT);
            }

            if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permisos.add(Manifest.permission.BLUETOOTH_SCAN);
            }

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }

            if (!permisos.isEmpty()) {
                requestPermissions(permisos.toArray(new String[0]), 1234);
            }
        }
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
    }
}
