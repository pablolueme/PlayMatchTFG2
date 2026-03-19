package com.example.proyectofinaltfg2.repository;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.model.UserProfile;
import com.example.proyectofinaltfg2.utils.FirebaseAuthUtil;
import com.example.proyectofinaltfg2.utils.ValidationUtils;
import com.google.firebase.auth.FirebaseUser;

public class UsuarioRepository {

    public static final String CIUDAD_FIJA = "Salamanca";

    public interface UsuarioResultCallback {
        void onSuccess(@NonNull UserProfile userProfile);

        void onError(@NonNull String errorMessage);
    }

    public interface RepositoryCallback {
        void onSuccess();

        void onError(@NonNull String errorMessage);
    }

    public void obtenerUsuarioActual(
            @NonNull Context context,
            @NonNull UsuarioResultCallback callback
    ) {
        FirebaseUser currentUser = FirebaseAuthUtil.getCurrentUser();
        if (currentUser == null) {
            callback.onError(context.getString(R.string.msg_auth_invalid_credentials));
            return;
        }

        FirebaseAuthUtil.getCurrentUserProfile(context, new FirebaseAuthUtil.UserProfileCallback() {
            @Override
            public void onSuccess(@NonNull UserProfile userProfile) {
                boolean normalized = normalizeUserProfile(userProfile, currentUser);
                if (normalized) {
                    saveSilently(userProfile, context);
                }
                callback.onSuccess(userProfile);
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                if (context.getString(R.string.msg_profile_not_found).equals(errorMessage)) {
                    UserProfile fallbackProfile = buildFallbackProfile(currentUser);
                    FirebaseAuthUtil.saveUserProfile(fallbackProfile, context, new FirebaseAuthUtil.AuthResultCallback() {
                        @Override
                        public void onSuccess() {
                            callback.onSuccess(fallbackProfile);
                        }

                        @Override
                        public void onError(@NonNull String saveError) {
                            // Devuelve igualmente datos base para no romper la UI de perfil.
                            callback.onSuccess(fallbackProfile);
                        }
                    });
                    return;
                }
                callback.onError(errorMessage);
            }
        });
    }

    public void actualizarNombreYNivelDeportivo(
            @NonNull Context context,
            @NonNull String nombre,
            int nivelDeportivo,
            @NonNull RepositoryCallback callback
    ) {
        String safeNombre = nombre.trim();
        if (TextUtils.isEmpty(safeNombre)) {
            callback.onError(context.getString(R.string.error_name_required));
            return;
        }
        if (nivelDeportivo < ValidationUtils.MIN_LEVEL || nivelDeportivo > ValidationUtils.MAX_LEVEL) {
            callback.onError(context.getString(R.string.error_profile_level_required));
            return;
        }

        obtenerUsuarioActual(context, new UsuarioResultCallback() {
            @Override
            public void onSuccess(@NonNull UserProfile userProfile) {
                userProfile.setNombre(safeNombre);
                userProfile.setCiudad(CIUDAD_FIJA);
                userProfile.setNivelDeportivo(nivelDeportivo);

                FirebaseAuthUtil.saveUserProfile(userProfile, context, new FirebaseAuthUtil.AuthResultCallback() {
                    @Override
                    public void onSuccess() {
                        callback.onSuccess();
                    }

                    @Override
                    public void onError(@NonNull String errorMessage) {
                        callback.onError(errorMessage);
                    }
                });
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    private void saveSilently(@NonNull UserProfile userProfile, @NonNull Context context) {
        FirebaseAuthUtil.saveUserProfile(userProfile, context, new FirebaseAuthUtil.AuthResultCallback() {
            @Override
            public void onSuccess() {
                // No-op.
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                // No-op.
            }
        });
    }

    private boolean normalizeUserProfile(
            @NonNull UserProfile userProfile,
            @NonNull FirebaseUser currentUser
    ) {
        boolean changed = false;

        if (TextUtils.isEmpty(userProfile.getUid())) {
            userProfile.setUid(currentUser.getUid());
            changed = true;
        }

        if (TextUtils.isEmpty(userProfile.getCorreo())) {
            userProfile.setCorreo(currentUser.getEmail());
            changed = true;
        }

        if (TextUtils.isEmpty(userProfile.getRol())) {
            userProfile.setRol(ValidationUtils.ROLE_USER);
            changed = true;
        }

        if (!CIUDAD_FIJA.equals(userProfile.getCiudad())) {
            userProfile.setCiudad(CIUDAD_FIJA);
            changed = true;
        }

        return changed;
    }

    @NonNull
    private UserProfile buildFallbackProfile(@NonNull FirebaseUser currentUser) {
        UserProfile fallbackProfile = new UserProfile();
        fallbackProfile.setUid(currentUser.getUid());
        fallbackProfile.setCorreo(currentUser.getEmail());
        fallbackProfile.setRol(ValidationUtils.ROLE_USER);
        fallbackProfile.setCiudad(CIUDAD_FIJA);
        fallbackProfile.setActivo(true);
        return fallbackProfile;
    }
}
