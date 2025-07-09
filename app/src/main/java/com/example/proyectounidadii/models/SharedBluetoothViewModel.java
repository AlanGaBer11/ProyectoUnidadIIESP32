package com.example.proyectounidadii.models;

import androidx.lifecycle.ViewModel;

public class SharedBluetoothViewModel extends ViewModel {
    // --- SENSOR DHT11 ---
    public static class DhtData {
        public final int temperatura; // °C
        public final int humedad;     // %

        public DhtData(int t, int h) {
            temperatura = t;
            humedad = h;
        }
    }

    // --- SENSOR FOTOSENSIBLE ---
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
        public final boolean detectado; // true / false

        public MovimientoData(boolean detectado) {
            this.detectado = detectado;
        }
    }
}
