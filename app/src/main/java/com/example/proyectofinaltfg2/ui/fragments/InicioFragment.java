package com.example.proyectofinaltfg2.ui.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.model.UserProfile;
import com.example.proyectofinaltfg2.repository.UsuarioRepository;
import com.example.proyectofinaltfg2.utils.ValidationUtils;

public class InicioFragment extends Fragment {

    private TextView txtAliasHome;
    private TextView txtEstadoHome;
    private UsuarioRepository usuarioRepository;

    public InicioFragment() {
        super(R.layout.home_layout);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        usuarioRepository = new UsuarioRepository();

        View bottomNav = view.findViewById(R.id.include_bottom_nav_home);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }

        txtAliasHome = view.findViewById(R.id.txt_alias_home);
        txtEstadoHome = view.findViewById(R.id.txt_estado_home);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileHeader();
    }

    private void loadProfileHeader() {
        if (!isAdded()) {
            return;
        }

        usuarioRepository.obtenerUsuarioActual(requireContext(), new UsuarioRepository.UsuarioResultCallback() {
            @Override
            public void onSuccess(@NonNull UserProfile userProfile) {
                bindProfile(
                        userProfile.getAlias(),
                        userProfile.getNombre(),
                        userProfile.getRol()
                );
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                txtAliasHome.setText(R.string.home_bienvenida_default);
                txtEstadoHome.setText(errorMessage);
            }
        });
    }

    private void bindProfile(
            @Nullable String alias,
            @Nullable String nombreCompleto,
            @Nullable String role
    ) {
        if (TextUtils.isEmpty(alias)) {
            txtAliasHome.setText(R.string.home_bienvenida_default);
        } else {
            txtAliasHome.setText(alias);
        }

        String safeNombre = TextUtils.isEmpty(nombreCompleto)
                ? getString(R.string.home_nombre_no_disponible)
                : nombreCompleto;
        String safeRole = TextUtils.isEmpty(role) ? ValidationUtils.ROLE_USER : role;
        txtEstadoHome.setText(getString(R.string.home_profile_detail, safeNombre, safeRole));
    }
}
