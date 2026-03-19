package com.example.proyectofinaltfg2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinaltfg2.model.UserProfile;
import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.utils.FirebaseAuthUtil;
import com.example.proyectofinaltfg2.utils.ValidationUtils;

public class LoginActivity extends AppCompatActivity {

    private EditText edtCorreoLogin;
    private EditText edtPasswordLogin;
    private Button btnIniciarSesion;
    private TextView txtIrRegistro;
    private TextView txtIrRecuperarContrasena;
    private ProgressBar progressLogin;
    private CheckBox chkRecordarSesion;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        bindViews();
        configureListeners();
        handleAutoLogin();
    }

    private void bindViews() {
        edtCorreoLogin = findViewById(R.id.edt_correo_login);
        edtPasswordLogin = findViewById(R.id.edt_password_login);
        btnIniciarSesion = findViewById(R.id.btn_iniciar_sesion);
        txtIrRegistro = findViewById(R.id.txt_ir_registro);
        txtIrRecuperarContrasena = findViewById(R.id.txt_ir_recuperar_contrasena);
        progressLogin = findViewById(R.id.progress_login);
        chkRecordarSesion = findViewById(R.id.chk_recordar_sesion);
    }

    private void configureListeners() {
        btnIniciarSesion.setOnClickListener(v -> attemptLogin());
        txtIrRegistro.setOnClickListener(v -> startActivity(new Intent(this, RegistroActivity.class)));
        txtIrRecuperarContrasena.setOnClickListener(
                v -> startActivity(new Intent(this, RecuperarContrasenaActivity.class))
        );
    }

    private void handleAutoLogin() {
        if (FirebaseAuthUtil.isUserLoggedIn()) {
            FirebaseAuthUtil.getCurrentUserProfile(this, new FirebaseAuthUtil.UserProfileCallback() {
                @Override
                public void onSuccess(@NonNull UserProfile userProfile) {
                    navigateToHome(userProfile);
                    finish();
                }

                @Override
                public void onError(@NonNull String errorMessage) {
                    FirebaseAuthUtil.logout();
                }
            });
        }
    }

    private void attemptLogin() {
        clearErrors();
        String email = textOf(edtCorreoLogin);
        String password = textOf(edtPasswordLogin);

        ValidationUtils.ValidationResult validationResult = ValidationUtils.validateLogin(email, password);
        if (!validationResult.isValid()) {
            showError(getString(validationResult.getMessageResId()), validationResult.getField());
            return;
        }

        setLoading(true);

        FirebaseAuthUtil.loginAndFetchProfile(
                email,
                password,
                this,
                new FirebaseAuthUtil.UserProfileCallback() {
            @Override
            public void onSuccess(@NonNull UserProfile userProfile) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, R.string.msg_login_success, Toast.LENGTH_SHORT).show();
                navigateToHome(userProfile);
                finish();
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                setLoading(false);
                FirebaseAuthUtil.logout();
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showError(@NonNull String message, @Nullable ValidationUtils.Field field) {
        if (field == null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return;
        }

        if (field == ValidationUtils.Field.EMAIL) {
            edtCorreoLogin.setError(message);
            edtCorreoLogin.requestFocus();
            return;
        }

        if (field == ValidationUtils.Field.PASSWORD) {
            edtPasswordLogin.setError(message);
            edtPasswordLogin.requestFocus();
            return;
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void clearErrors() {
        edtCorreoLogin.setError(null);
        edtPasswordLogin.setError(null);
    }

    private void setLoading(boolean isLoading) {
        progressLogin.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnIniciarSesion.setEnabled(!isLoading);
        txtIrRegistro.setEnabled(!isLoading);
        txtIrRecuperarContrasena.setEnabled(!isLoading);
        edtCorreoLogin.setEnabled(!isLoading);
        edtPasswordLogin.setEnabled(!isLoading);
        chkRecordarSesion.setEnabled(!isLoading);
    }

    @NonNull
    private String textOf(@NonNull EditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private void navigateToHome(@NonNull UserProfile userProfile) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(HomeActivity.EXTRA_ALIAS, userProfile.getAlias());
        intent.putExtra(HomeActivity.EXTRA_NOMBRE_COMPLETO, userProfile.getNombre());
        intent.putExtra(HomeActivity.EXTRA_ROLE, userProfile.getRol());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
