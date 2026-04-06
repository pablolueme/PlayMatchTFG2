package com.example.proyectofinaltfg2.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.model.UserProfile;
import com.example.proyectofinaltfg2.repository.UsuarioRepository;
import com.example.proyectofinaltfg2.ui.EditarPerfilActivity;
import com.example.proyectofinaltfg2.ui.HistorialPartidosActivity;
import com.example.proyectofinaltfg2.ui.LoginActivity;
import com.example.proyectofinaltfg2.utils.FirebaseAuthUtil;
import com.example.proyectofinaltfg2.utils.ValidationUtils;

public class PerfilFragment extends Fragment {

    private TextView txtNombrePerfil;
    private TextView txtCorreoPerfil;
    private TextView txtRolPerfil;
    private TextView txtCiudadPerfil;
    private TextView txtNivelPadelPerfil;
    private TextView txtNivelTenisPerfil;
    private Button btnEditarPerfil;
    private Button btnCerrarSesionPerfil;
    private LinearLayout cardHistorialPerfil;
    private LinearLayout cardAjustesPerfil;

    private UsuarioRepository usuarioRepository;

    public PerfilFragment() {
        super(R.layout.perfil_layout);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        usuarioRepository = new UsuarioRepository();

        View bottomNav = view.findViewById(R.id.include_bottom_nav_perfil);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }

        bindViews(view);
        configureListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void bindViews(@NonNull View view) {
        txtNombrePerfil = view.findViewById(R.id.txt_nombre_perfil);
        txtCorreoPerfil = view.findViewById(R.id.txt_correo_perfil);
        txtRolPerfil = view.findViewById(R.id.txt_rol_perfil);
        txtCiudadPerfil = view.findViewById(R.id.txt_ciudad_perfil);
        txtNivelPadelPerfil = view.findViewById(R.id.txt_nivel_padel_perfil);
        txtNivelTenisPerfil = view.findViewById(R.id.txt_nivel_tenis_perfil);
        btnEditarPerfil = view.findViewById(R.id.btn_editar_perfil);
        btnCerrarSesionPerfil = view.findViewById(R.id.btn_cerrar_sesion_perfil);
        cardHistorialPerfil = view.findViewById(R.id.card_historial_perfil);
        cardAjustesPerfil = view.findViewById(R.id.card_ajustes_perfil);
    }

    private void configureListeners() {
        btnEditarPerfil.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), EditarPerfilActivity.class))
        );
        btnCerrarSesionPerfil.setOnClickListener(v -> logoutAndNavigateToLogin());
        cardHistorialPerfil.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), HistorialPartidosActivity.class))
        );
        cardAjustesPerfil.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.msg_ajustes_proximamente, Toast.LENGTH_SHORT).show()
        );
    }

    private void loadUserProfile() {
        if (!isAdded()) {
            return;
        }

        usuarioRepository.obtenerUsuarioActual(requireContext(), new UsuarioRepository.UsuarioResultCallback() {
            @Override
            public void onSuccess(@NonNull UserProfile userProfile) {
                bindProfileData(userProfile);
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                bindFallbackData();
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindProfileData(@NonNull UserProfile userProfile) {
        String safeName = TextUtils.isEmpty(userProfile.getNombre())
                ? getString(R.string.home_nombre_no_disponible)
                : userProfile.getNombre();
        String safeEmail = TextUtils.isEmpty(userProfile.getCorreo())
                ? FirebaseAuthUtil.getCurrentUserEmail()
                : userProfile.getCorreo();
        String safeRole = TextUtils.isEmpty(userProfile.getRol())
                ? ValidationUtils.ROLE_USER
                : userProfile.getRol();

        if (TextUtils.isEmpty(safeEmail)) {
            safeEmail = getString(R.string.perfil_email_no_disponible);
        }

        txtNombrePerfil.setText(safeName);
        txtCorreoPerfil.setText(safeEmail);
        txtRolPerfil.setText(getString(R.string.perfil_rol_format, safeRole));
        txtCiudadPerfil.setText(getString(R.string.perfil_ciudad_format, UsuarioRepository.CIUDAD_FIJA));
        txtNivelPadelPerfil.setText(
                getString(
                        R.string.perfil_nivel_padel_format,
                        getNivelTexto(userProfile.getNivelPadel())
                )
        );
        txtNivelTenisPerfil.setText(
                getString(
                        R.string.perfil_nivel_tenis_format,
                        getNivelTexto(userProfile.getNivelTenis())
                )
        );
    }

    private void bindFallbackData() {
        String safeEmail = FirebaseAuthUtil.getCurrentUserEmail();
        if (TextUtils.isEmpty(safeEmail)) {
            safeEmail = getString(R.string.perfil_email_no_disponible);
        }

        txtNombrePerfil.setText(R.string.home_nombre_no_disponible);
        txtCorreoPerfil.setText(safeEmail);
        txtRolPerfil.setText(getString(R.string.perfil_rol_format, ValidationUtils.ROLE_USER));
        txtCiudadPerfil.setText(getString(R.string.perfil_ciudad_format, UsuarioRepository.CIUDAD_FIJA));
        txtNivelPadelPerfil.setText(
                getString(
                        R.string.perfil_nivel_padel_format,
                        getString(R.string.perfil_nivel_no_disponible)
                )
        );
        txtNivelTenisPerfil.setText(
                getString(
                        R.string.perfil_nivel_tenis_format,
                        getString(R.string.perfil_nivel_no_disponible)
                )
        );
    }

    @NonNull
    private String getNivelTexto(int nivel) {
        int nivelNormalizado = normalizeLevel(nivel);
        if (nivelNormalizado != -1) {
            return String.valueOf(nivelNormalizado);
        }
        return getString(R.string.perfil_nivel_no_disponible);
    }

    private int normalizeLevel(int level) {
        if (level < ValidationUtils.MIN_LEVEL || level > ValidationUtils.MAX_LEVEL) {
            return -1;
        }
        return level;
    }

    private void logoutAndNavigateToLogin() {
        FirebaseAuthUtil.logout();
        Toast.makeText(requireContext(), R.string.msg_session_closed, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
