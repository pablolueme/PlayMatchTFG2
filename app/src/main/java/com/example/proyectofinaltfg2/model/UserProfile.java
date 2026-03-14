package com.example.proyectofinaltfg2.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class UserProfile {

    private String uid;
    private String nombreCompleto;
    private String alias;
    private String email;
    private String role;
    private int nivelPadel;
    private int nivelTenis;

    public UserProfile() {
        // Constructor vacio requerido por Firestore
    }

    public UserProfile(
            @Nullable String uid,
            @Nullable String nombreCompleto,
            @Nullable String alias,
            @Nullable String email,
            @Nullable String role,
            int nivelPadel,
            int nivelTenis
    ) {
        this.uid = uid == null ? "" : uid;
        this.nombreCompleto = nombreCompleto == null ? "" : nombreCompleto;
        this.alias = alias == null ? "" : alias;
        this.email = email == null ? "" : email;
        this.role = role == null ? "" : role;
        this.nivelPadel = nivelPadel;
        this.nivelTenis = nivelTenis;
    }

    @NonNull
    public String getUid() {
        return uid == null ? "" : uid;
    }

    public void setUid(@Nullable String uid) {
        this.uid = uid == null ? "" : uid;
    }

    @NonNull
    public String getNombreCompleto() {
        return nombreCompleto == null ? "" : nombreCompleto;
    }

    public void setNombreCompleto(@Nullable String nombreCompleto) {
        this.nombreCompleto = nombreCompleto == null ? "" : nombreCompleto;
    }

    @NonNull
    public String getAlias() {
        return alias == null ? "" : alias;
    }

    public void setAlias(@Nullable String alias) {
        this.alias = alias == null ? "" : alias;
    }

    @NonNull
    public String getEmail() {
        return email == null ? "" : email;
    }

    public void setEmail(@Nullable String email) {
        this.email = email == null ? "" : email;
    }

    @NonNull
    public String getRole() {
        return role == null ? "" : role;
    }

    public void setRole(@Nullable String role) {
        this.role = role == null ? "" : role;
    }

    public int getNivelPadel() {
        return nivelPadel;
    }

    public void setNivelPadel(int nivelPadel) {
        this.nivelPadel = nivelPadel;
    }

    public int getNivelTenis() {
        return nivelTenis;
    }

    public void setNivelTenis(int nivelTenis) {
        this.nivelTenis = nivelTenis;
    }
}
