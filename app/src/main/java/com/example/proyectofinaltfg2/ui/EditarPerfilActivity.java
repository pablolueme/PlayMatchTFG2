package com.example.proyectofinaltfg2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.model.UserProfile;
import com.example.proyectofinaltfg2.repository.UsuarioRepository;
import com.example.proyectofinaltfg2.utils.FirebaseAuthUtil;
import com.example.proyectofinaltfg2.utils.ValidationUtils;

import java.util.Arrays;
import java.util.List;

public class EditarPerfilActivity extends AppCompatActivity {

    private static final List<String> OPCIONES_NIVEL = Arrays.asList("1", "2", "3", "4", "5");

    private EditText edtNombreEditarPerfil;
    private EditText edtCiudadEditarPerfil;
    private Spinner spinnerNivelPadelEditarPerfil;
    private Spinner spinnerNivelTenisEditarPerfil;
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
        configurarSpinnersNivel();
        lockCityField();
        configureListeners();
        loadCurrentProfile();
    }

    private void bindViews() {
        edtNombreEditarPerfil = findViewById(R.id.edt_nombre_editar_perfil);
        edtCiudadEditarPerfil = findViewById(R.id.edt_ciudad_editar_perfil);
        spinnerNivelPadelEditarPerfil = findViewById(R.id.spinner_nivel_padel_editar_perfil);
        spinnerNivelTenisEditarPerfil = findViewById(R.id.spinner_nivel_tenis_editar_perfil);
        btnGuardarEditarPerfil = findViewById(R.id.btn_guardar_editar_perfil);
        btnCancelarEditarPerfil = findViewById(R.id.btn_cancelar_editar_perfil);
    }

    private void configurarSpinnersNivel() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                OPCIONES_NIVEL
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerNivelPadelEditarPerfil.setAdapter(adapter);
        spinnerNivelTenisEditarPerfil.setAdapter(adapter);
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
        seleccionarNivelEnSpinner(spinnerNivelPadelEditarPerfil, userProfile.getNivelPadel());
        seleccionarNivelEnSpinner(spinnerNivelTenisEditarPerfil, userProfile.getNivelTenis());
    }

    private void attemptSaveProfile() {
        if (isSaving) {
            return;
        }

        clearErrors();

        String nombre = textOf(edtNombreEditarPerfil);
        int nivelPadel = obtenerNivelSeleccionado(spinnerNivelPadelEditarPerfil);
        int nivelTenis = obtenerNivelSeleccionado(spinnerNivelTenisEditarPerfil);

        if (ValidationUtils.isFieldEmpty(nombre)) {
            edtNombreEditarPerfil.setError(getString(R.string.error_name_required));
            edtNombreEditarPerfil.requestFocus();
            return;
        }

        setLoading(true);
        usuarioRepository.actualizarNombreYNivelesDeportivos(
                this,
                nombre.trim(),
                nivelPadel,
                nivelTenis,
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
        spinnerNivelPadelEditarPerfil.setEnabled(!loading);
        spinnerNivelTenisEditarPerfil.setEnabled(!loading);
        btnGuardarEditarPerfil.setEnabled(!loading);
        btnCancelarEditarPerfil.setEnabled(!loading);
    }

    private void clearErrors() {
        edtNombreEditarPerfil.setError(null);
    }

    @NonNull
    private String textOf(@NonNull EditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    @NonNull
    private void seleccionarNivelEnSpinner(@NonNull Spinner spinner, int nivel) {
        if (nivel < ValidationUtils.MIN_LEVEL || nivel > ValidationUtils.MAX_LEVEL) {
            spinner.setSelection(0);
            return;
        }
        String valorBuscado = String.valueOf(nivel);
        for (int i = 0; i < spinner.getCount(); i++) {
            Object item = spinner.getItemAtPosition(i);
            if (item != null && valorBuscado.equals(String.valueOf(item))) {
                spinner.setSelection(i);
                return;
            }
        }
        spinner.setSelection(0);
    }

    private int obtenerNivelSeleccionado(@NonNull Spinner spinner) {
        Object itemSeleccionado = spinner.getSelectedItem();
        if (itemSeleccionado == null) {
            return ValidationUtils.MIN_LEVEL;
        }
        return ValidationUtils.parseLevel(String.valueOf(itemSeleccionado));
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
