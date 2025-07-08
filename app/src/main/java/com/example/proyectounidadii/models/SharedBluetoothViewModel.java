package com.example.proyectounidadii.models;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import androidx.annotation.RequiresPermission;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SharedBluetoothViewModel extends ViewModel {
    // --- ESTADO DEL BLUETOOTH (Desconectado, Conectado, Conectando...) ---
    private final MutableLiveData<String> estado = new MutableLiveData<>("Desconectado");
    // --- DATOS PARA LOS SENSORES ---
    private final MutableLiveData<DhtData> dht = new MutableLiveData<>();
    private final MutableLiveData<MovimientoData> movimiento = new MutableLiveData<>(); // DISTANCIA EN CENTIMETORS
    private final MutableLiveData<FotosensibleData> fotosensible = new MutableLiveData<>(); // MQ-2 PPM (PARTS PER MILLION)
    private BluetoothSocket socket;
    private InputStream in;
    private Thread rxThread;
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // --- GETTERS ---
    public LiveData<String> getEstado() {
        return estado;
    }

    public LiveData<DhtData> getDht() {
        return dht;
    }

    public LiveData<MovimientoData> getMovimiento() {
        return movimiento;
    }

    public LiveData<FotosensibleData> getFotosensible() {
        return fotosensible;
    }


    // --- METODO PARA CONECTAR EL BLUETOOTH ---
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT})
    public void conectar(BluetoothDevice device) {
        if (rxThread != null) rxThread.interrupt();
        estado.postValue("Conectando…");

        rxThread = new Thread(() -> {
            try {
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                socket.connect();
                in = socket.getInputStream();
                estado.postValue("Conectado a " + device.getName() + " ...");
                leerDatos();
            } catch (IOException e) {
                estado.postValue("Error de conexión");
            }
        });
        rxThread.start();
    }

    // --- METODO PARA LECTURA Y PARSEO DE DATOS DEL BLUETOOTH ---
    private void leerDatos() {
        byte[] buffer = new byte[1024];
        StringBuilder sb = new StringBuilder();

        while (!Thread.currentThread().isInterrupted()) {
            try {
                int n = in.read(buffer);
                if (n > 0) {
                    sb.append(new String(buffer, 0, n));
                    int idx;
                    while ((idx = sb.indexOf("#")) != -1) {  // MARCO DE FIN DE TRAMA ‘#’
                        String trama = sb.substring(0, idx);
                        procesarTrama(trama);
                    }
                }
            } catch (IOException e) {
                estado.postValue("Desconectado");
                break;
            }
        }
    }

    // --- METODO PARA PROCESAR LA TRAMA DEL BLUETOOTH ERROR ---
    private void procesarTrama(String t) {
        if (t.startsWith("$DHT")) {
            Pattern p = Pattern.compile("HUM:(\\d+)%.*TEMP:\\s*(\\d+)");
            Matcher m = p.matcher(t);
            if (m.find()) {
                int hum = Integer.parseInt(m.group(1));
                int temp = Integer.parseInt(m.group(2));
                dht.postValue(new DhtData(temp, hum));
            }
        } else if (t.startsWith("$LDR")) {
            Pattern p = Pattern.compile("Valor:(\\d+);\\s*Luz:(\\d+)%");
            Matcher m = p.matcher(t);
            if (m.find()) {
                int valor = Integer.parseInt(m.group(1));
                int luz = Integer.parseInt(m.group(2));
                fotosensible.postValue(new FotosensibleData(valor, luz));
            }
        } else if (t.startsWith("$MOV")) {
            if (t.contains("Detectado")) {
                movimiento.postValue(new MovimientoData(true));
            } else if (t.contains("No Detectado")) {
                movimiento.postValue(new MovimientoData(false));
            }
        }
    }


    // --- METODO PARA DESCONECTAR EL BLUETOOTH ---
    public void desconectar() {
        estado.postValue("Desconectado");
        if (rxThread != null) rxThread.interrupt();
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        desconectar();
    }

    /* --- MODELO DE DATOS PARA SENSORES --- */
    // --- SENSOR DHT11 ---
    public static class DhtData {
        public final int temperatura; // °C
        public final int humedad;     // %

        public DhtData(int t, int h) {
            temperatura = t;
            humedad = h;
        }
    }

    // --- SENSOR FOTOSENSIBLE
    public static class FotosensibleData {
        public final int valorRaw;  // Valor analógico
        public final int luz;       // Porcentaje

        public FotosensibleData(int valor, int luz) {
            this.valorRaw = valor;
            this.luz = luz;
        }
    }

    // --- SENSOR DE MOVIMIENTO ---
    public static class MovimientoData {
        public final boolean detectado; // SI ? NO

        public MovimientoData(boolean detectado) {
            this.detectado = detectado;
        }
    }

}