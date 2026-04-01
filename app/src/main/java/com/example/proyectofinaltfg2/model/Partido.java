package com.example.proyectofinaltfg2.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Partido {

    public static final String DEPORTE_PADEL = "padel";
    public static final String DEPORTE_TENIS = "tenis";

    public static final String ESTADO_ACTIVO = "ACTIVO";
    public static final String ESTADO_COMPLETO = "COMPLETO";
    public static final String ESTADO_FINALIZADO = "FINALIZADO";
    public static final String ESTADO_CANCELADO = "CANCELADO";

    private String idPartido;
    private String deporte;
    private String fecha;
    private String hora;
    private String direccion;
    private String nivel;
    private String creadorId;
    private String creadorNombre;
    private List<String> participantes;
    private int acompanantesIniciales;
    private int maxJugadores;
    private int plazasOcupadas;
    private String estado;
    @Nullable
    private Resultado resultado;
    private boolean resultadoConfirmado;
    @Nullable
    private Date fechaCreacion;
    @Nullable
    private Date ultimaActualizacion;

    public Partido() {
        this.idPartido = "";
        this.deporte = "";
        this.fecha = "";
        this.hora = "";
        this.direccion = "";
        this.nivel = "";
        this.creadorId = "";
        this.creadorNombre = "";
        this.participantes = new ArrayList<>();
        this.acompanantesIniciales = 0;
        this.maxJugadores = 0;
        this.plazasOcupadas = 0;
        this.estado = ESTADO_ACTIVO;
        this.resultado = null;
        this.resultadoConfirmado = false;
        this.fechaCreacion = null;
        this.ultimaActualizacion = null;
    }

    @NonNull
    public String getIdPartido() {
        return sanitize(idPartido);
    }

    public void setIdPartido(@Nullable String idPartido) {
        this.idPartido = sanitize(idPartido);
    }

    @NonNull
    public String getDeporte() {
        return sanitize(deporte);
    }

    public void setDeporte(@Nullable String deporte) {
        this.deporte = sanitize(deporte);
    }

    @NonNull
    public String getFecha() {
        return sanitize(fecha);
    }

    public void setFecha(@Nullable String fecha) {
        this.fecha = sanitize(fecha);
    }

    @NonNull
    public String getHora() {
        return sanitize(hora);
    }

    public void setHora(@Nullable String hora) {
        this.hora = sanitize(hora);
    }

    @NonNull
    public String getDireccion() {
        return sanitize(direccion);
    }

    public void setDireccion(@Nullable String direccion) {
        this.direccion = sanitize(direccion);
    }

    @NonNull
    public String getNivel() {
        return sanitize(nivel);
    }

    public void setNivel(@Nullable String nivel) {
        this.nivel = sanitize(nivel);
    }

    @NonNull
    public String getCreadorId() {
        return sanitize(creadorId);
    }

    public void setCreadorId(@Nullable String creadorId) {
        this.creadorId = sanitize(creadorId);
    }

    @NonNull
    public String getCreadorNombre() {
        return sanitize(creadorNombre);
    }

    public void setCreadorNombre(@Nullable String creadorNombre) {
        this.creadorNombre = sanitize(creadorNombre);
    }

    @NonNull
    public List<String> getParticipantes() {
        return new ArrayList<>(participantes);
    }

    public void setParticipantes(@Nullable List<String> participantes) {
        this.participantes = new ArrayList<>();
        if (participantes == null) {
            return;
        }
        for (String participanteId : participantes) {
            String safeId = sanitize(participanteId);
            if (!safeId.isEmpty() && !this.participantes.contains(safeId)) {
                this.participantes.add(safeId);
            }
        }
    }

    public int getAcompanantesIniciales() {
        return acompanantesIniciales;
    }

    public void setAcompanantesIniciales(int acompanantesIniciales) {
        this.acompanantesIniciales = Math.max(acompanantesIniciales, 0);
    }

    public int getMaxJugadores() {
        return maxJugadores;
    }

    public void setMaxJugadores(int maxJugadores) {
        this.maxJugadores = maxJugadores;
    }

    public int getPlazasOcupadas() {
        return plazasOcupadas;
    }

    public void setPlazasOcupadas(int plazasOcupadas) {
        this.plazasOcupadas = plazasOcupadas;
    }

    @NonNull
    public String getEstado() {
        return sanitize(estado);
    }

    public void setEstado(@Nullable String estado) {
        this.estado = sanitize(estado);
    }

    @Nullable
    public Resultado getResultado() {
        return resultado;
    }

    public void setResultado(@Nullable Resultado resultado) {
        this.resultado = resultado;
    }

    public boolean isResultadoConfirmado() {
        return resultadoConfirmado || (resultado != null && resultado.isConfirmado());
    }

    public void setResultadoConfirmado(boolean resultadoConfirmado) {
        this.resultadoConfirmado = resultadoConfirmado;
    }

    @Nullable
    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(@Nullable Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Nullable
    public Date getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(@Nullable Date ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public int getPlazasLibres() {
        return Math.max(maxJugadores - plazasOcupadas, 0);
    }

    public boolean estaCompleto() {
        int max = Math.max(getMaxJugadores(), 0);
        if (max == 0) {
            return false;
        }
        return Math.max(getPlazasOcupadas(), 0) >= max;
    }

    public boolean estaFinalizado() {
        return ESTADO_FINALIZADO.equals(getEstado());
    }

    public boolean participaUsuario(@Nullable String usuarioId) {
        String usuarioSafe = sanitize(usuarioId);
        if (usuarioSafe.isEmpty()) {
            return false;
        }
        if (usuarioSafe.equals(getCreadorId())) {
            return true;
        }
        return getParticipantes().contains(usuarioSafe);
    }

    public boolean tieneResultadoConfirmado() {
        return isResultadoConfirmado();
    }

    @NonNull
    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("idPartido", getIdPartido());
        data.put("deporte", getDeporte());
        data.put("fecha", getFecha());
        data.put("hora", getHora());
        data.put("direccion", getDireccion());
        data.put("nivel", getNivel());
        data.put("creadorId", getCreadorId());
        data.put("creadorNombre", getCreadorNombre());
        data.put("participantes", getParticipantes());
        data.put("acompanantesIniciales", getAcompanantesIniciales());
        data.put("maxJugadores", getMaxJugadores());
        data.put("plazasOcupadas", getPlazasOcupadas());
        data.put("estado", getEstado());
        data.put("resultado", resultado == null ? null : resultado.toMap());
        data.put("resultadoConfirmado", isResultadoConfirmado());
        data.put("fechaCreacion", getFechaCreacion());
        data.put("ultimaActualizacion", getUltimaActualizacion());
        return data;
    }

    @Nullable
    public static Partido fromDocument(@NonNull DocumentSnapshot documentSnapshot) {
        if (!documentSnapshot.exists()) {
            return null;
        }

        Partido partido = new Partido();
        partido.setIdPartido(readString(documentSnapshot, "idPartido"));
        if (partido.getIdPartido().isEmpty()) {
            partido.setIdPartido(documentSnapshot.getId());
        }
        partido.setDeporte(readString(documentSnapshot, "deporte"));
        partido.setFecha(readString(documentSnapshot, "fecha"));
        partido.setHora(readString(documentSnapshot, "hora"));
        partido.setDireccion(readString(documentSnapshot, "direccion"));
        partido.setNivel(readString(documentSnapshot, "nivel"));
        partido.setCreadorId(readString(documentSnapshot, "creadorId"));
        partido.setCreadorNombre(readString(documentSnapshot, "creadorNombre"));
        partido.setParticipantes(readStringList(documentSnapshot.get("participantes")));
        partido.setAcompanantesIniciales(readInt(documentSnapshot, "acompanantesIniciales"));
        partido.setMaxJugadores(readInt(documentSnapshot, "maxJugadores"));
        partido.setPlazasOcupadas(readInt(documentSnapshot, "plazasOcupadas"));
        partido.setEstado(readString(documentSnapshot, "estado"));
        partido.setFechaCreacion(documentSnapshot.getDate("fechaCreacion"));
        partido.setUltimaActualizacion(documentSnapshot.getDate("ultimaActualizacion"));
        boolean confirmadoDocumento = Boolean.TRUE.equals(
                documentSnapshot.getBoolean("resultadoConfirmado")
        );

        Object resultadoRaw = documentSnapshot.get("resultado");
        if (resultadoRaw instanceof Map) {
            //noinspection unchecked
            partido.setResultado(Resultado.fromMap((Map<String, Object>) resultadoRaw));
        }
        boolean confirmadoResultado = partido.getResultado() != null
                && partido.getResultado().isConfirmado();
        partido.setResultadoConfirmado(confirmadoDocumento || confirmadoResultado);

        if (Partido.DEPORTE_PADEL.equals(partido.getDeporte())
                && partido.getAcompanantesIniciales() == 0
                && partido.getPlazasOcupadas() > 1) {
            partido.setAcompanantesIniciales(Math.max(partido.getPlazasOcupadas() - 1, 0));
        }

        return partido;
    }

    @NonNull
    private static List<String> readStringList(@Nullable Object rawValue) {
        List<String> values = new ArrayList<>();
        if (!(rawValue instanceof List)) {
            return values;
        }

        List<?> rawList = (List<?>) rawValue;
        for (Object rawItem : rawList) {
            if (rawItem == null) {
                continue;
            }
            String safeValue = sanitize(String.valueOf(rawItem));
            if (!safeValue.isEmpty()) {
                values.add(safeValue);
            }
        }
        return values;
    }

    private static int readInt(@NonNull DocumentSnapshot snapshot, @NonNull String key) {
        Long longValue = snapshot.getLong(key);
        if (longValue == null) {
            return 0;
        }
        return longValue.intValue();
    }

    @NonNull
    private static String readString(@NonNull DocumentSnapshot snapshot, @NonNull String key) {
        String value = snapshot.getString(key);
        return sanitize(value);
    }

    @NonNull
    private static String sanitize(@Nullable String value) {
        return value == null ? "" : value.trim();
    }
}
