package com.example.proyectounidadii.models;

import androidx.lifecycle.ViewModel;

/**
 * ViewModel que define estructuras de datos inmutables (POJOs) utilizadas para
 * transportar información de sensores y actuadores a través de LiveData.
 *
 * No mantiene estado propio ni lógica, simplemente agrupa *value objects*
 * fuertemente tipados para: DHT11, LDR y LED.
 */
public class SharedBluetoothViewModel extends ViewModel {

    /* ==================== SENSOR DHT11 (Temperatura & Humedad) ==================== */
    public static class DhtData {
        public final int temperatura; // Temperatura en °C
        public final int humedad;     // Humedad relativa en %

        public DhtData(int t, int h) {
            temperatura = t;
            humedad = h;
        }
    }

    /* ==================== SENSOR FOTOSENSIBLE (LDR) ==================== */
    public static class FotosensibleData {
        public final int valorRaw;  // Lectura analógica cruda (0‑1095 típicamente)
        public final int luz;       // Luz en porcentaje (%) ya procesada en Arduino

        public FotosensibleData(int valor, int luz) {
            this.valorRaw = valor;
            this.luz = luz;
        }
    }

    /* ==================== ACTUADOR: LED ==================== */
    public static class LedData {
        public final boolean encendido; // true = LED encendido, false = apagado

        public LedData(boolean encendido) {
            this.encendido = encendido;
        }
    }
}
