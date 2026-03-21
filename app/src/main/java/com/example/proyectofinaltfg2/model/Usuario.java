package com.example.proyectofinaltfg2.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Usuario {

    private String id;
    private String nombre;
    private String alias;
    private String correo;

    public Usuario() {
        this.id = "";
        this.nombre = "";
        this.alias = "";
        this.correo = "";
    }

    public Usuario(
            @Nullable String id,
            @Nullable String nombre,
            @Nullable String alias,
            @Nullable String correo
    ) {
        this.id = sanitize(id);
        this.nombre = sanitize(nombre);
        this.alias = sanitize(alias);
        this.correo = sanitize(correo);
    }

    @NonNull
    public String getId() {
        return sanitize(id);
    }

    public void setId(@Nullable String id) {
        this.id = sanitize(id);
    }

    @NonNull
    public String getNombre() {
        return sanitize(nombre);
    }

    public void setNombre(@Nullable String nombre) {
        this.nombre = sanitize(nombre);
    }

    @NonNull
    public String getAlias() {
        return sanitize(alias);
    }

    public void setAlias(@Nullable String alias) {
        this.alias = sanitize(alias);
    }

    @NonNull
    public String getCorreo() {
        return sanitize(correo);
    }

    public void setCorreo(@Nullable String correo) {
        this.correo = sanitize(correo);
    }

    @NonNull
    public String getNombreMostrado() {
        if (!getAlias().isEmpty()) {
            return getAlias();
        }
        if (!getNombre().isEmpty()) {
            return getNombre();
        }
        return getCorreo();
    }

    @NonNull
    public static Usuario fromUserProfile(@NonNull UserProfile userProfile) {
        return new Usuario(
                userProfile.getUid(),
                userProfile.getNombre(),
                userProfile.getAlias(),
                userProfile.getCorreo()
        );
    }

    @NonNull
    private static String sanitize(@Nullable String value) {
        return value == null ? "" : value.trim();
    }
}
