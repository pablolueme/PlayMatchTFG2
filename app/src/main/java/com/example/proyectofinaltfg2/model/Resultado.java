package com.example.proyectofinaltfg2.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class Resultado {

    private String marcador;

    public Resultado() {
        this.marcador = "";
    }

    public Resultado(@Nullable String marcador) {
        this.marcador = sanitize(marcador);
    }

    @NonNull
    public String getMarcador() {
        return sanitize(marcador);
    }

    public void setMarcador(@Nullable String marcador) {
        this.marcador = sanitize(marcador);
    }

    @NonNull
    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("marcador", getMarcador());
        return data;
    }

    @Nullable
    public static Resultado fromMap(@Nullable Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        Object marcadorValue = map.get("marcador");
        if (marcadorValue == null) {
            return new Resultado();
        }
        return new Resultado(String.valueOf(marcadorValue));
    }

    @NonNull
    private static String sanitize(@Nullable String value) {
        return value == null ? "" : value.trim();
    }
}
