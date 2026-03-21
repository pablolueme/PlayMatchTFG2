package com.example.proyectofinaltfg2.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.proyectofinaltfg2.utils.FirebaseAuthUtil;
import com.google.firebase.auth.FirebaseUser;

public class AuthService {

    public boolean isUsuarioAutenticado() {
        return FirebaseAuthUtil.isUserLoggedIn();
    }

    @NonNull
    public String getUsuarioIdActual() {
        FirebaseUser firebaseUser = FirebaseAuthUtil.getCurrentUser();
        if (firebaseUser == null || firebaseUser.getUid() == null) {
            return "";
        }
        return firebaseUser.getUid().trim();
    }

    @Nullable
    public FirebaseUser getUsuarioActual() {
        return FirebaseAuthUtil.getCurrentUser();
    }
}
