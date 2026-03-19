package com.example.proyectofinaltfg2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.model.UserProfile;
import com.example.proyectofinaltfg2.repository.UsuarioRepository;
import com.example.proyectofinaltfg2.utils.FirebaseAuthUtil;
import com.example.proyectofinaltfg2.utils.ValidationUtils;

public class EditarPerfilActivity extends AppCompatActivity {

    private EditText edtNombreEditarPerfil;
    private EditText edtCiudadEditarPerfil;
    private EditText edtNivelEditarPerfil;
    private Button btnGuardarEditarPerfil;
    private Button btnCancelarEditarPerfil;

    private UsuarioRepository usuarioRepository;
    private boolean isSaving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editar_perfil_layout);

        if (!FirebaseAuthUtil.isUserLoggedIn()) {
            navigateToLogin();
            finish();
            return;
        }

        usuarioRepository = new UsuarioRepository();
        bindViews();
        lockCityField();
        configureListeners();
        loadCurrentProfile();
    }

    private void bindViews() {
        edtNombreEditarPerfil = findViewById(R.id.edt_nombre_editar_perfil);
        edtCiudadEditarPerfil = findViewById(R.id.edt_ciudad_editar_perfil);
        edtNivelEditarPerfil = findViewById(R.id.edt_nivel_editar_perfil);
        btnGuardarEditarPerfil = findViewById(R.id.btn_guardar_editar_perfil);
        btnCancelarEditarPerfil = findViewById(R.id.btn_cancelar_editar_perfil);
    }

    private void lockCityField() {
        edtCiudadEditarPerfil.setText(UsuarioRepository.CIUDAD_FIJA);
        edtCiudadEditarPerfil.setFocusable(false);
        edtCiudadEditarPerfil.setFocusableInTouchMode(false);
        edtCiudadEditarPerfil.setClickable(false);
        edtCiudadEditarPerfil.setLongClickable(false);
        edtCiudadEditarPerfil.setCursorVisible(false);
    }

    private void configureListeners() {
        btnGuardarEditarPerfil.setOnClickListener(v -> attemptSaveProfile());
        btnCancelarEditarPerfil.setOnClickListener(v -> finish());
    }

    private void loadCurrentProfile() {
        setLoading(true);
        usuarioRepository.obtenerUsuarioActual(this, new UsuarioRepository.UsuarioResultCallback() {
            @Override
            public void onSuccess(@NonNull UserProfile userProfile) {
                setLoading(false);
                bindEditableData(userProfile);
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                setLoading(false);
                Toast.makeText(EditarPerfilActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindEditableData(@NonNull UserProfile userProfile) {
        edtNombreEditarPerfil.setText(userProfile.getNombre());
        edtCiudadEditarPerfil.setText(UsuarioRepository.CIUDAD_FIJA);
        edtNivelEditarPerfil.setText(resolveEditableLevel(userProfile));
    }

    private void attemptSaveProfile() {
        if (isSaving) {
            return;
        }

        clearErrors();

        String nombre = textOf(edtNombreEditarPerfil);
        String nivel = textOf(edtNivelEditarPerfil);

        if (ValidationUtils.isFieldEmpty(nombre)) {
            edtNombreEditarPerfil.setError(getString(R.string.error_name_required));
            edtNombreEditarPerfil.requestFocus();
            return;
        }

        if (!ValidationUtils.isValidLevelSelection(nivel)) {
            edtNivelEditarPerfil.setError(getString(R.string.error_profile_level_required));
            edtNivelEditarPerfil.requestFocus();
            return;
        }

        int parsedLevel = ValidationUtils.parseLevel(nivel);
        if (parsedLevel < ValidationUtils.MIN_LEVEL || parsedLevel > ValidationUtils.MAX_LEVEL) {
            edtNivelEditarPerfil.setError(getString(R.string.error_profile_level_required));
            edtNivelEditarPerfil.requestFocus();
            return;
        }

        setLoading(true);
        usuarioRepository.actualizarNombreYNivelDeportivo(
                this,
                nombre.trim(),
                parsedLevel,
                new UsuarioRepository.RepositoryCallback() {
                    @Override
                    public void onSuccess() {
                        setLoading(false);
                        Toast.makeText(
                                EditarPerfilActivity.this,
                                R.string.msg_profile_updated,
                                Toast.LENGTH_SHORT
                        ).show();
                        setResult(RESULT_OK);
                        finish();
                    }

                    @Override
                    public void onError(@NonNull String errorMessage) {
                        setLoading(false);
                        Toast.makeText(EditarPerfilActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setLoading(boolean loading) {
        isSaving = loading;
        edtNombreEditarPerfil.setEnabled(!loading);
        edtNivelEditarPerfil.setEnabled(!loading);
        btnGuardarEditarPerfil.setEnabled(!loading);
        btnCancelarEditarPerfil.setEnabled(!loading);
    }

    private void clearErrors() {
        edtNombreEditarPerfil.setError(null);
        edtNivelEditarPerfil.setError(null);
    }

    @NonNull
    private String textOf(@NonNull EditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    @NonNull
    private String resolveEditableLevel(@NonNull UserProfile userProfile) {
        int nivelDeportivo = userProfile.getNivelDeportivo();
        if (nivelDeportivo >= ValidationUtils.MIN_LEVEL
                && nivelDeportivo <= ValidationUtils.MAX_LEVEL) {
            return String.valueOf(nivelDeportivo);
        }
        return "";
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
