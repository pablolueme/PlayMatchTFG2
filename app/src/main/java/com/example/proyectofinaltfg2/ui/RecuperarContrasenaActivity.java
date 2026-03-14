package com.example.proyectofinaltfg2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.utils.FirebaseAuthUtil;
import com.example.proyectofinaltfg2.utils.ValidationUtils;

public class RecuperarContrasenaActivity extends AppCompatActivity {

    private EditText edtCorreoRecuperacion;
    private Button btnEnviarEnlace;
    private TextView txtVolverLogin;
    private ProgressBar progressRecuperacion;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recuperar_contrasena_layout);

        bindViews();
        configureListeners();
    }

    private void bindViews() {
        edtCorreoRecuperacion = findViewById(R.id.edt_correo_recuperacion);
        btnEnviarEnlace = findViewById(R.id.btn_enviar_enlace);
        txtVolverLogin = findViewById(R.id.txt_volver_login);
        progressRecuperacion = findViewById(R.id.progress_recuperacion);
    }

    private void configureListeners() {
        btnEnviarEnlace.setOnClickListener(v -> attemptSendRecoveryEmail());
        txtVolverLogin.setOnClickListener(v -> {
            navigateToLogin();
            finish();
        });
    }

    private void attemptSendRecoveryEmail() {
        clearErrors();
        String email = textOf(edtCorreoRecuperacion);

        ValidationUtils.ValidationResult validationResult = ValidationUtils.validateRecoveryEmail(email);
        if (!validationResult.isValid()) {
            showError(getString(validationResult.getMessageResId()), validationResult.getField());
            return;
        }

        setLoading(true);
        FirebaseAuthUtil.sendPasswordResetEmail(email, this, new FirebaseAuthUtil.AuthResultCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                Toast.makeText(
                        RecuperarContrasenaActivity.this,
                        R.string.msg_recovery_email_sent,
                        Toast.LENGTH_LONG
                ).show();
                navigateToLogin();
                finish();
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                setLoading(false);
                Toast.makeText(RecuperarContrasenaActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showError(@NonNull String message, @Nullable ValidationUtils.Field field) {
        if (field == ValidationUtils.Field.EMAIL) {
            edtCorreoRecuperacion.setError(message);
            edtCorreoRecuperacion.requestFocus();
            return;
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void clearErrors() {
        edtCorreoRecuperacion.setError(null);
    }

    private void setLoading(boolean isLoading) {
        progressRecuperacion.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnEnviarEnlace.setEnabled(!isLoading);
        txtVolverLogin.setEnabled(!isLoading);
        edtCorreoRecuperacion.setEnabled(!isLoading);
    }

    @NonNull
    private String textOf(@NonNull EditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
