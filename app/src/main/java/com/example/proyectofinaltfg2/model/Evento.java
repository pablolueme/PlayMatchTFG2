package com.example.proyectofinaltfg2.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Evento {

    public static final String ESTADO_PUBLICADO = "PUBLICADO";

    private String idEvento;
    private String titulo;
    private String descripcion;
    private String fecha;
    private String hora;
    private String ubicacion;
    private String clubId;
    private String clubNombre;
    private String estadoPublicacion;
    @Nullable
    private Date fechaCreacion;
    @Nullable
    private Date ultimaActualizacion;

    public Evento() {
        this.idEvento = "";
        this.titulo = "";
        this.descripcion = "";
        this.fecha = "";
        this.hora = "";
        this.ubicacion = "";
        this.clubId = "";
        this.clubNombre = "";
        this.estadoPublicacion = ESTADO_PUBLICADO;
        this.fechaCreacion = null;
        this.ultimaActualizacion = null;
    }

    @NonNull
    public String getIdEvento() {
        return sanitize(idEvento);
    }

    public void setIdEvento(@Nullable String idEvento) {
        this.idEvento = sanitize(idEvento);
    }

    @NonNull
    public String getTitulo() {
        return sanitize(titulo);
    }

    public void setTitulo(@Nullable String titulo) {
        this.titulo = sanitize(titulo);
    }

    @NonNull
    public String getDescripcion() {
        return sanitize(descripcion);
    }

    public void setDescripcion(@Nullable String descripcion) {
        this.descripcion = sanitize(descripcion);
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
    public String getUbicacion() {
        return sanitize(ubicacion);
    }

    public void setUbicacion(@Nullable String ubicacion) {
        this.ubicacion = sanitize(ubicacion);
    }

    @NonNull
    public String getClubId() {
        return sanitize(clubId);
    }

    public void setClubId(@Nullable String clubId) {
        this.clubId = sanitize(clubId);
    }

    @NonNull
    public String getClubNombre() {
        return sanitize(clubNombre);
    }

    public void setClubNombre(@Nullable String clubNombre) {
        this.clubNombre = sanitize(clubNombre);
    }

    @NonNull
    public String getEstadoPublicacion() {
        return sanitize(estadoPublicacion);
    }

    public void setEstadoPublicacion(@Nullable String estadoPublicacion) {
        this.estadoPublicacion = sanitize(estadoPublicacion);
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

    public boolean estaPublicado() {
        return ESTADO_PUBLICADO.equals(getEstadoPublicacion());
    }

    public boolean esDelClub(@Nullable String uidClub) {
        String uidSeguro = sanitize(uidClub);
        if (uidSeguro.isEmpty()) {
            return false;
        }
        return uidSeguro.equals(getClubId());
    }

    @NonNull
    public Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("idEvento", getIdEvento());
        data.put("titulo", getTitulo());
        data.put("descripcion", getDescripcion());
        data.put("fecha", getFecha());
        data.put("hora", getHora());
        data.put("ubicacion", getUbicacion());
        data.put("clubId", getClubId());
        data.put("clubNombre", getClubNombre());
        data.put("estadoPublicacion", getEstadoPublicacion());
        data.put("fechaCreacion", getFechaCreacion());
        data.put("ultimaActualizacion", getUltimaActualizacion());
        return data;
    }

    @Nullable
    public static Evento fromDocument(@NonNull DocumentSnapshot documentSnapshot) {
        if (!documentSnapshot.exists()) {
            return null;
        }

        Evento evento = new Evento();
        evento.setIdEvento(readString(documentSnapshot, "idEvento"));
        if (evento.getIdEvento().isEmpty()) {
            evento.setIdEvento(documentSnapshot.getId());
        }
        evento.setTitulo(readString(documentSnapshot, "titulo"));
        evento.setDescripcion(readString(documentSnapshot, "descripcion"));
        evento.setFecha(readString(documentSnapshot, "fecha"));
        evento.setHora(readString(documentSnapshot, "hora"));
        evento.setUbicacion(readString(documentSnapshot, "ubicacion"));
        evento.setClubId(readString(documentSnapshot, "clubId"));
        evento.setClubNombre(readString(documentSnapshot, "clubNombre"));
        evento.setEstadoPublicacion(readString(documentSnapshot, "estadoPublicacion"));
        evento.setFechaCreacion(documentSnapshot.getDate("fechaCreacion"));
        evento.setUltimaActualizacion(documentSnapshot.getDate("ultimaActualizacion"));
        if (evento.getEstadoPublicacion().isEmpty()) {
            evento.setEstadoPublicacion(ESTADO_PUBLICADO);
        }
        return evento;
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
