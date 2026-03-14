package com.example.proyectofinaltfg2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinaltfg2.model.UserProfile;
import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.utils.FirebaseAuthUtil;
import com.example.proyectofinaltfg2.utils.ValidationUtils;

public class HomeActivity extends AppCompatActivity {

    public static final String EXTRA_ALIAS = "extra_alias";
    public static final String EXTRA_NOMBRE_COMPLETO = "extra_nombre_completo";
    public static final String EXTRA_ROLE = "extra_role";

    private TextView txtAliasHome;
    private TextView txtEstadoHome;
    private Button btnCerrarSesion;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_layout);

        if (!FirebaseAuthUtil.isUserLoggedIn()) {
            navigateToLogin();
            finish();
            return;
        }

        bindViews();
        setupProfileHeader();
        configureListeners();
    }

    private void bindViews() {
        txtAliasHome = findViewById(R.id.txt_alias_home);
        txtEstadoHome = findViewById(R.id.txt_estado_home);
        btnCerrarSesion = findViewById(R.id.btn_cerrar_sesion);
    }

    private void setupProfileHeader() {
        String alias = getIntent().getStringExtra(EXTRA_ALIAS);
        String nombreCompleto = getIntent().getStringExtra(EXTRA_NOMBRE_COMPLETO);
        String role = getIntent().getStringExtra(EXTRA_ROLE);

        if (!TextUtils.isEmpty(alias)) {
            bindProfile(alias, nombreCompleto, role);
            return;
        }

        FirebaseAuthUtil.getCurrentUserProfile(this, new FirebaseAuthUtil.UserProfileCallback() {
            @Override
            public void onSuccess(@NonNull UserProfile userProfile) {
                bindProfile(
                        userProfile.getAlias(),
                        userProfile.getNombreCompleto(),
                        userProfile.getRole()
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

    private void configureListeners() {
        btnCerrarSesion.setOnClickListener(v -> {
            FirebaseAuthUtil.logout();
            Toast.makeText(this, R.string.msg_session_closed, Toast.LENGTH_SHORT).show();
            navigateToLogin();
            finish();
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
