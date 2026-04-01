package com.example.proyectofinaltfg2.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Resultado {

    private String idResultado;
    private String marcador;
    private boolean confirmado;
    private String set1;
    private String set2;
    private String set3;
    private String tieBreakSet1;
    private String tieBreakSet2;
    private String tieBreakSet3;

    public Resultado() {
        this.idResultado = "";
        this.marcador = "";
        this.confirmado = false;
        this.set1 = "";
        this.set2 = "";
        this.set3 = "";
        this.tieBreakSet1 = "";
        this.tieBreakSet2 = "";
        this.tieBreakSet3 = "";
    }

    public Resultado(@Nullable String marcador) {
        this();
        this.marcador = sanitize(marcador);
    }

    @NonNull
    public String getIdResultado() {
        return sanitize(idResultado);
    }

    public void setIdResultado(@Nullable String idResultado) {
        this.idResultado = sanitize(idResultado);
    }

    @NonNull
    public String getMarcador() {
        String marcadorGuardado = sanitize(marcador);
        if (!marcadorGuardado.isEmpty()) {
            return marcadorGuardado;
        }
        return construirMarcadorResumen();
    }

    public void setMarcador(@Nullable String marcador) {
        this.marcador = sanitize(marcador);
    }

    public void actualizarMarcadorDesdeSets() {
        this.marcador = construirMarcadorResumen();
    }

    @NonNull
    public String getSet1() {
        return sanitize(set1);
    }

    public void setSet1(@Nullable String set1) {
        this.set1 = sanitize(set1);
    }

    @NonNull
    public String getSet2() {
        return sanitize(set2);
    }

    public void setSet2(@Nullable String set2) {
        this.set2 = sanitize(set2);
    }

    @NonNull
    public String getSet3() {
        return sanitize(set3);
    }

    public void setSet3(@Nullable String set3) {
        this.set3 = sanitize(set3);
    }

    @NonNull
    public String getTieBreakSet1() {
        return sanitize(tieBreakSet1);
    }

    public void setTieBreakSet1(@Nullable String tieBreakSet1) {
        this.tieBreakSet1 = sanitize(tieBreakSet1);
    }

    @NonNull
    public String getTieBreakSet2() {
        return sanitize(tieBreakSet2);
    }

    public void setTieBreakSet2(@Nullable String tieBreakSet2) {
        this.tieBreakSet2 = sanitize(tieBreakSet2);
    }

    @NonNull
    public String getTieBreakSet3() {
        return sanitize(tieBreakSet3);
    }

    public void setTieBreakSet3(@Nullable String tieBreakSet3) {
        this.tieBreakSet3 = sanitize(tieBreakSet3);
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public void setConfirmado(boolean confirmado) {
        this.confirmado = confirmado;
    }

    @NonNull
    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("idResultado", getIdResultado());
        data.put("marcador", getMarcador());
        data.put("set1", getSet1());
        data.put("set2", getSet2());
        data.put("set3", getSet3());
        data.put("tieBreakSet1", getTieBreakSet1());
        data.put("tieBreakSet2", getTieBreakSet2());
        data.put("tieBreakSet3", getTieBreakSet3());
        data.put("confirmado", isConfirmado());
        return data;
    }

    @Nullable
    public static Resultado fromMap(@Nullable Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        Resultado resultado = new Resultado();
        Object idResultadoValue = map.get("idResultado");
        Object marcadorValue = map.get("marcador");
        Object set1Value = map.get("set1");
        Object set2Value = map.get("set2");
        Object set3Value = map.get("set3");
        Object tieBreakSet1Value = map.get("tieBreakSet1");
        Object tieBreakSet2Value = map.get("tieBreakSet2");
        Object tieBreakSet3Value = map.get("tieBreakSet3");
        Object confirmadoValue = map.get("confirmado");

        if (idResultadoValue != null) {
            resultado.setIdResultado(String.valueOf(idResultadoValue));
        }
        if (marcadorValue != null) {
            resultado.setMarcador(String.valueOf(marcadorValue));
        }
        if (set1Value != null) {
            resultado.setSet1(String.valueOf(set1Value));
        }
        if (set2Value != null) {
            resultado.setSet2(String.valueOf(set2Value));
        }
        if (set3Value != null) {
            resultado.setSet3(String.valueOf(set3Value));
        }
        if (tieBreakSet1Value != null) {
            resultado.setTieBreakSet1(String.valueOf(tieBreakSet1Value));
        }
        if (tieBreakSet2Value != null) {
            resultado.setTieBreakSet2(String.valueOf(tieBreakSet2Value));
        }
        if (tieBreakSet3Value != null) {
            resultado.setTieBreakSet3(String.valueOf(tieBreakSet3Value));
        }
        resultado.setConfirmado(parseBoolean(confirmadoValue));
        if (resultado.getMarcador().isEmpty()) {
            resultado.actualizarMarcadorDesdeSets();
        }

        return resultado;
    }

    @NonNull
    private String construirMarcadorResumen() {
        List<String> sets = new ArrayList<>();
        agregarSetSiNoVacio(sets, getSet1(), getTieBreakSet1());
        agregarSetSiNoVacio(sets, getSet2(), getTieBreakSet2());
        agregarSetSiNoVacio(sets, getSet3(), getTieBreakSet3());
        return String.join(" ", sets).trim();
    }

    private void agregarSetSiNoVacio(
            @NonNull List<String> sets,
            @NonNull String set,
            @NonNull String tieBreak
    ) {
        if (set.isEmpty()) {
            return;
        }
        if (!tieBreak.isEmpty() && requiereTieBreak(set)) {
            sets.add(set + " (" + tieBreak + ")");
            return;
        }
        sets.add(set);
    }

    private boolean requiereTieBreak(@NonNull String set) {
        return "7-6".equals(set) || "6-7".equals(set);
    }

    private static boolean parseBoolean(@Nullable Object rawValue) {
        if (rawValue instanceof Boolean) {
            return (Boolean) rawValue;
        }
        if (rawValue == null) {
            return false;
        }
        return Boolean.parseBoolean(String.valueOf(rawValue));
    }

    @NonNull
    private static String sanitize(@Nullable String value) {
        return value == null ? "" : value.trim();
    }
}
