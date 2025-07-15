package com.example.proyectounidadii;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.proyectounidadii.databinding.ActivityMainBinding;
import com.example.proyectounidadii.models.SharedBluetoothViewModel;
import com.example.proyectounidadii.ui.listado.ListadoFragment;
import com.example.proyectounidadii.ui.sensores.SensoresFragment;
import com.example.proyectounidadii.ui.led.LedFragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
        private ActivityMainBinding binding;

    /* ====================== Componentes Bluetooth ====================== */
    private BluetoothSocket socket;             // Socket RFCOMM para la comunicación
    private InputStream in;                     // Flujo de entrada (leer)
    private OutputStream out;                   // Flujo de salida (escribir)
    private Thread rxThread;                    // Hilo dedicado a la recepción
    // UUID estándar para el perfil Serial Port Profile (SPP)
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothDevice dispositivoGuardado; // Último dispositivo conectado (para reconexión)

    /* ======================== LiveData compartidos ======================== */
    // Estado de la conexión ("Conectando…", "Conectado", "Desconectado", etc.)
    private final MutableLiveData<String> estado = new MutableLiveData<>("Desconectado");
    // Datos del sensor DHT
    private final MutableLiveData<SharedBluetoothViewModel.DhtData> dht = new MutableLiveData<>();
    // Datos del sensor fotosensible (LDR)
    private final MutableLiveData<SharedBluetoothViewModel.FotosensibleData> fotosensible = new MutableLiveData<>();
    // Estado del LED
    private final MutableLiveData<SharedBluetoothViewModel.LedData> led = new MutableLiveData<>();

    /* ===================================================================== */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflamos la vista y la asociamos a la actividad
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1) Solicitar permisos necesarios para BLE (a partir de Android 12 son obligatorios)
        pedirPermisosBluetooth();

        // 2) Navegación: si es la primera vez, mostramos ListadoFragment por defecto
        if (savedInstanceState == null) {
            showFragment(new ListadoFragment());
        } else {
            // Restablecemos el fragment previamente visible tras configuración/rotación
            String fragmentTag = savedInstanceState.getString("currentFragment");
            if ("second".equals(fragmentTag)) {
                showFragment(new SensoresFragment());
            } else if ("third".equals(fragmentTag)) {
                showFragment(new LedFragment());
            } else {
                showFragment(new ListadoFragment());
            }

            // Recuperamos la dirección MAC del dispositivo anteriormente conectado
            String deviceAddress = savedInstanceState.getString("deviceAddress");
            if (deviceAddress != null) {
                dispositivoGuardado = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
            }
        }

        /* =================== Listeners de la barra de navegación =================== */
        binding.btnListado.setOnClickListener(view -> showFragment(new ListadoFragment()));
        binding.btnSensores.setOnClickListener(view -> showFragment(new SensoresFragment()));
        binding.btnLed.setOnClickListener(view -> showFragment(new LedFragment()));
    }

    /**
     * Guardamos el estado para restaurarlo tras cambios de configuración
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        // Identificamos qué fragment estaba visible
        if (currentFragment instanceof ListadoFragment) {
            outState.putString("currentFragment", "first");
        } else if (currentFragment instanceof SensoresFragment) {
            outState.putString("currentFragment", "second");
        } else if (currentFragment instanceof LedFragment) {
            outState.putString("currentFragment", "third");
        }

        // Guardamos también la MAC del dispositivo para reconectar automáticamente
        if (dispositivoGuardado != null) {
            outState.putString("deviceAddress", dispositivoGuardado.getAddress());
        }
    }

    /* ================= Ciclo de vida para gestionar reconexión ================= */

    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    @Override
    protected void onStart() {
        super.onStart();
        // Si salimos y volvemos a la actividad, intentamos reconectar
        if (dispositivoGuardado != null && (socket == null || !socket.isConnected())) {
            Toast.makeText(this, "Reconectando a " + dispositivoGuardado.getName(), Toast.LENGTH_SHORT).show();
            conectar(dispositivoGuardado);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Cerramos la conexión para liberar recursos cuando la actividad deja de ser visible
        Toast.makeText(this, "Cerrando conexión Bluetooth en onStop()", Toast.LENGTH_SHORT).show();
        desconectar();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpieza final
        Toast.makeText(this, "onDestroy() ejecutado", Toast.LENGTH_SHORT).show();
        desconectar();
    }

    /* ====================== Navegación entre Fragments ====================== */
    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    /* =================== Solicitud dinámica de permisos =================== */
    private void pedirPermisosBluetooth() {
        // A partir de Android 12 (API 31) se requieren permisos explícitos
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

            // Si alguno falta, lo solicitamos en lote (requestCode = 1234)
            if (!permisos.isEmpty()) {
                requestPermissions(permisos.toArray(new String[0]), 1234);
            }
        }
    }

    /* ===================== Conexión Bluetooth clásica ===================== */
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    public void conectar(BluetoothDevice device) {
        // Si ya hay un hilo RX corriendo, lo detenemos
        if (rxThread != null) rxThread.interrupt();
        estado.postValue("Conectando...");
        dispositivoGuardado = device; // Guardamos referencia para futuras reconexiones

        rxThread = new Thread(() -> {
            try {
                // 1) Cerramos socket previo si estuviera abierto
                if (socket != null && socket.isConnected()) {
                    socket.close();
                }

                // 2) Creamos un nuevo socket RFCOMM y nos conectamos
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery(); // Importante antes de conectar
                socket.connect();

                // 3) Si la conexión fue exitosa, obtenemos streams y empezamos a leer
                if (socket.isConnected()) {
                    in = socket.getInputStream();
                    out = socket.getOutputStream();
                    estado.postValue("Conectado a " + device.getName());
                    leerDatos(); // Bucle de lectura (bloqueante)
                } else {
                    estado.postValue("Error de conexión");
                }

            } catch (IOException e) {
                e.printStackTrace();
                estado.postValue("Error de conexión");
            }
        });
        rxThread.start();
    }

    /* ======= Getters para exponer el socket y el OutputStream a otros Fragments ======= */
    public BluetoothSocket getSocket() { return socket; }
    public OutputStream getOutputStream() { return out; }

    /* ====================== Lectura asíncrona de datos ====================== */
    private void leerDatos() {
        byte[] buffer = new byte[1024];
        StringBuilder sb = new StringBuilder();

        // Bucle infinito (hasta que el hilo sea interrumpido o ocurra error)
        while (!Thread.currentThread().isInterrupted()) {
            try {
                int n = in.read(buffer); // Lectura bloqueante
                if (n > 0) {
                    sb.append(new String(buffer, 0, n));
                    int idx;
                    // Las tramas están delimitadas por el carácter '#'
                    while ((idx = sb.indexOf("#")) != -1) {
                        String trama = sb.substring(0, idx);
                        procesarTrama(trama);
                        sb.delete(0, idx + 1); // Eliminamos la trama procesada
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                estado.postValue("Desconectado");
                break; // Salimos del bucle
            }
        }
    }

    /* =================== Decodificación de tramas recibidas =================== */
    private void procesarTrama(String t) {
        Log.d("TRAMA_BT", "Trama recibida: " + t);

        if (t.startsWith("$DHT")) {
            // Ejemplo: $DHT Humedad: 60%; Temperatura: 25C°
            Pattern p = Pattern.compile("Humedad:\\s*(\\d+)%;\\s*Temperatura:\\s*(\\d+)C°");
            Matcher m = p.matcher(t);
            if (m.find()) {
                int hum = Integer.parseInt(m.group(1));
                int temp = Integer.parseInt(m.group(2));
                dht.postValue(new SharedBluetoothViewModel.DhtData(temp, hum));
                Log.d("DHT_DATA", "Temp=" + temp + ", Hum=" + hum);
            }

        } else if (t.startsWith("$LDR")) {
            // Ejemplo: $LDR Valor: 123; Luz: 80%
            Pattern p = Pattern.compile("Valor:\\s*(\\d+);\\s*Luz:\\s*(\\d+)%");
            Matcher m = p.matcher(t);
            if (m.find()) {
                int valor = Integer.parseInt(m.group(1));
                int luz = Integer.parseInt(m.group(2));
                fotosensible.postValue(new SharedBluetoothViewModel.FotosensibleData(valor, luz));
                Log.d("LDR_DATA", "Raw=" + valor + ", Luz=" + luz);
            }

        } else if (t.startsWith("$LED")) {
            // Ejemplo: $LED Encendido  or  $LED Apagado
            if (t.contains("Encendido")) {
                led.postValue(new SharedBluetoothViewModel.LedData(true));
                Log.d("LED_DATA", "LED Encendido");
            } else if (t.contains("Apagado")) {
                led.postValue(new SharedBluetoothViewModel.LedData(false));
                Log.d("LED_DATA", "LED Apagado");
            }
        }
    }

    /* ======================= Cierre de la conexión ======================= */
    public void desconectar() {
        estado.postValue("Desconectado");
        if (rxThread != null) rxThread.interrupt();
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) { }
    }

    /* ===== Getters para que los Fragments observen los LiveData fácilmente ===== */
    public LiveData<String> getEstado() { return estado; }
    public LiveData<SharedBluetoothViewModel.DhtData> getDht() { return dht; }
    public LiveData<SharedBluetoothViewModel.FotosensibleData> getFotosensible() { return fotosensible; }
    public LiveData<SharedBluetoothViewModel.LedData> getLed() { return led; }
}
