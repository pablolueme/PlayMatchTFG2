package com.example.proyectofinaltfg2.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class UserProfile {

    private String uid;
    private String nombre;
    private String correo;
    private String rol;
    private String ciudad;
    private int nivelDeportivo;
    private String nombreClub;
    private String descripcionClub;
    private String fotoPerfilUrl;
    private String fechaRegistro;
    private boolean activo;
    private String alias;

    public UserProfile() {
        // Constructor vacio requerido por Firestore
    }

    // Constructor legado compatible con el flujo actual de registro/login.
    public UserProfile(
            @Nullable String uid,
            @Nullable String nombreCompleto,
            @Nullable String alias,
            @Nullable String email,
            @Nullable String role,
            int nivelPadel,
            int nivelTenis
    ) {
        this(
                uid,
                nombreCompleto,
                alias,
                email,
                role,
                "",
                "",
                nivelPadel,
                nivelTenis
        );
    }

    // Constructor legado compatible con la version previa del proyecto.
    public UserProfile(
            @Nullable String uid,
            @Nullable String nombreCompleto,
            @Nullable String alias,
            @Nullable String email,
            @Nullable String role,
            @Nullable String ciudad,
            @Nullable String fotoPerfilUrl,
            int nivelPadel,
            int nivelTenis
    ) {
        this.uid = sanitize(uid);
        this.nombre = sanitize(nombreCompleto);
        this.alias = sanitize(alias);
        this.correo = sanitize(email);
        this.rol = sanitize(role);
        this.ciudad = sanitize(ciudad);
        this.fotoPerfilUrl = sanitize(fotoPerfilUrl);
        this.nivelDeportivo = resolveNivelDeportivo(nivelPadel, nivelTenis);
        this.nombreClub = "";
        this.descripcionClub = "";
        this.fechaRegistro = "";
        this.activo = true;
    }

    @NonNull
    public String getUid() {
        return sanitize(uid);
    }

    public void setUid(@Nullable String uid) {
        this.uid = sanitize(uid);
    }

    @NonNull
    public String getNombre() {
        return sanitize(nombre);
    }

    public void setNombre(@Nullable String nombre) {
        this.nombre = sanitize(nombre);
    }

    @NonNull
    public String getCorreo() {
        return sanitize(correo);
    }

    public void setCorreo(@Nullable String correo) {
        this.correo = sanitize(correo);
    }

    @NonNull
    public String getRol() {
        return sanitize(rol);
    }

    public void setRol(@Nullable String rol) {
        this.rol = sanitize(rol);
    }

    @NonNull
    public String getCiudad() {
        return sanitize(ciudad);
    }

    public void setCiudad(@Nullable String ciudad) {
        this.ciudad = sanitize(ciudad);
    }

    public int getNivelDeportivo() {
        return nivelDeportivo;
    }

    public void setNivelDeportivo(int nivelDeportivo) {
        this.nivelDeportivo = nivelDeportivo;
    }

    @NonNull
    public String getNombreClub() {
        return sanitize(nombreClub);
    }

    public void setNombreClub(@Nullable String nombreClub) {
        this.nombreClub = sanitize(nombreClub);
    }

    @NonNull
    public String getDescripcionClub() {
        return sanitize(descripcionClub);
    }

    public void setDescripcionClub(@Nullable String descripcionClub) {
        this.descripcionClub = sanitize(descripcionClub);
    }

    @NonNull
    public String getFotoPerfilUrl() {
        return sanitize(fotoPerfilUrl);
    }

    public void setFotoPerfilUrl(@Nullable String fotoPerfilUrl) {
        this.fotoPerfilUrl = sanitize(fotoPerfilUrl);
    }

    @NonNull
    public String getFechaRegistro() {
        return sanitize(fechaRegistro);
    }

    public void setFechaRegistro(@Nullable String fechaRegistro) {
        this.fechaRegistro = sanitize(fechaRegistro);
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @NonNull
    public String getAlias() {
        return sanitize(alias);
    }

    public void setAlias(@Nullable String alias) {
        this.alias = sanitize(alias);
    }

    // Compatibilidad con nombres antiguos en UI/layers previas.
    @NonNull
    public String getNombreCompleto() {
        return getNombre();
    }

    public void setNombreCompleto(@Nullable String nombreCompleto) {
        setNombre(nombreCompleto);
    }

    @NonNull
    public String getEmail() {
        return getCorreo();
    }

    public void setEmail(@Nullable String email) {
        setCorreo(email);
    }

    @NonNull
    public String getRole() {
        return getRol();
    }

    public void setRole(@Nullable String role) {
        setRol(role);
    }

    public int getNivelPadel() {
        return getNivelDeportivo();
    }

    public void setNivelPadel(int nivelPadel) {
        setNivelDeportivo(nivelPadel);
    }

    public int getNivelTenis() {
        return getNivelDeportivo();
    }

    public void setNivelTenis(int nivelTenis) {
        setNivelDeportivo(nivelTenis);
    }

    @NonNull
    public Map<String, Object> toFirestoreMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("uid", getUid());
        data.put("nombre", getNombre());
        data.put("correo", getCorreo());
        data.put("rol", getRol());
        data.put("ciudad", getCiudad());
        data.put("nivelDeportivo", getNivelDeportivo());
        data.put("nombreClub", getNombreClub());
        data.put("descripcionClub", getDescripcionClub());
        data.put("fotoPerfilUrl", getFotoPerfilUrl());
        data.put("fechaRegistro", getFechaRegistro());
        data.put("activo", isActivo());
        if (!getAlias().isEmpty()) {
            data.put("alias", getAlias());
        }
        return data;
    }

    @Nullable
    public static UserProfile fromDocumentSnapshot(@NonNull DocumentSnapshot documentSnapshot) {
        if (!documentSnapshot.exists()) {
            return null;
        }

        UserProfile userProfile = new UserProfile();
        userProfile.setUid(readString(documentSnapshot, "uid", null));
        userProfile.setNombre(readString(documentSnapshot, "nombre", "nombreCompleto"));
        userProfile.setCorreo(readString(documentSnapshot, "correo", "email"));
        userProfile.setRol(readString(documentSnapshot, "rol", "role"));
        userProfile.setCiudad(readString(documentSnapshot, "ciudad", null));
        userProfile.setNivelDeportivo(readNivelDeportivo(documentSnapshot));
        userProfile.setNombreClub(readString(documentSnapshot, "nombreClub", null));
        userProfile.setDescripcionClub(readString(documentSnapshot, "descripcionClub", null));
        userProfile.setFotoPerfilUrl(readString(documentSnapshot, "fotoPerfilUrl", null));
        userProfile.setFechaRegistro(readString(documentSnapshot, "fechaRegistro", null));

        Boolean activoValue = documentSnapshot.getBoolean("activo");
        userProfile.setActivo(activoValue == null || activoValue);

        userProfile.setAlias(readString(documentSnapshot, "alias", null));
        if (userProfile.getUid().isEmpty()) {
            userProfile.setUid(documentSnapshot.getId());
        }

        return userProfile;
    }

    private static int readNivelDeportivo(@NonNull DocumentSnapshot snapshot) {
        Long nivel = snapshot.getLong("nivelDeportivo");
        if (nivel != null) {
            return nivel.intValue();
        }

        Long nivelPadel = snapshot.getLong("nivelPadel");
        Long nivelTenis = snapshot.getLong("nivelTenis");

        if (nivelPadel != null && nivelPadel > 0) {
            return nivelPadel.intValue();
        }
        if (nivelTenis != null && nivelTenis > 0) {
            return nivelTenis.intValue();
        }
        return -1;
    }

    @NonNull
    private static String readString(
            @NonNull DocumentSnapshot snapshot,
            @NonNull String primaryKey,
            @Nullable String legacyKey
    ) {
        String value = snapshot.getString(primaryKey);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        if (legacyKey == null) {
            return "";
        }
        String legacyValue = snapshot.getString(legacyKey);
        if (legacyValue == null) {
            return "";
        }
        return legacyValue.trim();
    }

    private static int resolveNivelDeportivo(int nivelPadel, int nivelTenis) {
        if (nivelPadel > 0) {
            return nivelPadel;
        }
        if (nivelTenis > 0) {
            return nivelTenis;
        }
        return -1;
    }

    @NonNull
    private static String sanitize(@Nullable String value) {
        return value == null ? "" : value.trim();
    }
}
