package com.example.proyectounidadii;

import android.os.Bundle;

import com.example.proyectounidadii.databinding.ActivityMainBinding;
import com.example.proyectounidadii.ui.dht.Dht11Fragment;
import com.example.proyectounidadii.ui.fotosensible.FotosensibleFragment;
import com.example.proyectounidadii.ui.movimiento.MovimientoFragment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new Dht11Fragment()).commit();

        binding.btnDht11.setOnClickListener(view -> showFragment(new Dht11Fragment()));
        binding.btnMovement.setOnClickListener(view -> showFragment(new MovimientoFragment()));
        binding.btnPhoto.setOnClickListener(view -> showFragment(new FotosensibleFragment()));

    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
    }
}
