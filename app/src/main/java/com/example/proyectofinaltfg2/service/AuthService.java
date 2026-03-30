package com.example.proyectofinaltfg2.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.proyectofinaltfg2.utils.FirebaseAuthUtil;
import com.google.firebase.auth.FirebaseUser;

public class AuthService {

    interface AuthGateway {
        boolean isUserLoggedIn();

        @Nullable
        FirebaseUser getCurrentUser();
    }

    private static final class FirebaseAuthGateway implements AuthGateway {
        @Override
        public boolean isUserLoggedIn() {
            return FirebaseAuthUtil.isUserLoggedIn();
        }

        @Nullable
        @Override
        public FirebaseUser getCurrentUser() {
            return FirebaseAuthUtil.getCurrentUser();
        }
    }

    private final AuthGateway authGateway;

    public AuthService() {
        this(new FirebaseAuthGateway());
    }

    AuthService(@NonNull AuthGateway authGateway) {
        this.authGateway = authGateway;
    }

    public boolean isUsuarioAutenticado() {
        return authGateway.isUserLoggedIn();
    }

    @NonNull
    public String getUsuarioIdActual() {
        FirebaseUser firebaseUser = authGateway.getCurrentUser();
        if (firebaseUser == null || firebaseUser.getUid() == null) {
            return "";
        }
        return firebaseUser.getUid().trim();
    }

    @Nullable
    public FirebaseUser getUsuarioActual() {
        return authGateway.getCurrentUser();
    }
}
