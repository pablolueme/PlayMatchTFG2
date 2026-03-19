package com.example.proyectofinaltfg2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinaltfg2.model.UserProfile;
import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.repository.UsuarioRepository;
import com.example.proyectofinaltfg2.utils.FirebaseAuthUtil;
import com.example.proyectofinaltfg2.utils.ValidationUtils;

import java.util.Arrays;
import java.util.List;

public class RegistroActivity extends AppCompatActivity {

    private static final List<String> LEVEL_OPTIONS = Arrays.asList("1", "2", "3", "4", "5");

    private EditText edtNombreRegistro;
    private EditText edtAliasRegistro;
    private EditText edtCorreoRegistro;
    private EditText edtPasswordRegistro;
    private EditText edtConfirmarPasswordRegistro;
    private EditText edtCodigoClub;
    private AutoCompleteTextView actvNivelPadel;
    private AutoCompleteTextView actvNivelTenis;
    private Button btnSelectorUser;
    private Button btnSelectorClub;
    private Button btnCrearCuenta;
    private LinearLayout layoutCodigoClub;
    private TextView txtIrLogin;
    private ProgressBar progressRegistro;

    private String selectedRole = ValidationUtils.ROLE_USER;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro_layout);

        bindViews();
        setupRoleSelector();
        setupLevelSelector(actvNivelPadel);
        setupLevelSelector(actvNivelTenis);
        configureListeners();
    }

    private void bindViews() {
        edtNombreRegistro = findViewById(R.id.edt_nombre_registro);
        edtAliasRegistro = findViewById(R.id.edt_alias_registro);
        edtCorreoRegistro = findViewById(R.id.edt_correo_registro);
        edtPasswordRegistro = findViewById(R.id.edt_password_registro);
        edtConfirmarPasswordRegistro = findViewById(R.id.edt_confirmar_password_registro);
        edtCodigoClub = findViewById(R.id.edt_codigo_club);
        actvNivelPadel = findViewById(R.id.actv_nivel_padel);
        actvNivelTenis = findViewById(R.id.actv_nivel_tenis);
        btnSelectorUser = findViewById(R.id.btn_selector_user);
        btnSelectorClub = findViewById(R.id.btn_selector_club);
        btnCrearCuenta = findViewById(R.id.btn_crear_cuenta);
        layoutCodigoClub = findViewById(R.id.layout_codigo_club);
        txtIrLogin = findViewById(R.id.txt_ir_login);
        progressRegistro = findViewById(R.id.progress_registro);
    }

    private void setupRoleSelector() {
        selectRole(ValidationUtils.ROLE_USER);
    }

    private void setupLevelSelector(@NonNull AutoCompleteTextView autoCompleteTextView) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                LEVEL_OPTIONS
        );
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setInputType(InputType.TYPE_NULL);
        autoCompleteTextView.setKeyListener(null);
        autoCompleteTextView.setOnClickListener(v -> autoCompleteTextView.showDropDown());
        autoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                autoCompleteTextView.showDropDown();
            }
        });
    }

    private void configureListeners() {
        btnSelectorUser.setOnClickListener(v -> selectRole(ValidationUtils.ROLE_USER));
        btnSelectorClub.setOnClickListener(v -> selectRole(ValidationUtils.ROLE_CLUB));
        btnCrearCuenta.setOnClickListener(v -> attemptRegister());
        txtIrLogin.setOnClickListener(v -> {
            navigateToLogin();
            finish();
        });
    }

    private void attemptRegister() {
        clearErrors();

        String nombre = textOf(edtNombreRegistro);
        String alias = textOf(edtAliasRegistro);
        String email = textOf(edtCorreoRegistro);
        String password = textOf(edtPasswordRegistro);
        String confirmarPassword = textOf(edtConfirmarPasswordRegistro);
        String codigoClub = textOf(edtCodigoClub);
        String nivelPadelText = textOf(actvNivelPadel);
        String nivelTenisText = textOf(actvNivelTenis);

        ValidationUtils.ValidationResult validationResult = ValidationUtils.validateRegister(
                nombre,
                alias,
                email,
                password,
                confirmarPassword,
                selectedRole,
                codigoClub,
                nivelPadelText,
                nivelTenisText
        );
        if (!validationResult.isValid()) {
            showError(getString(validationResult.getMessageResId()), validationResult.getField());
            return;
        }

        UserProfile userProfile = new UserProfile(
                "",
                nombre,
                alias,
                email,
                selectedRole,
                UsuarioRepository.CIUDAD_FIJA,
                "",
                ValidationUtils.parseLevel(nivelPadelText),
                ValidationUtils.parseLevel(nivelTenisText)
        );

        setLoading(true);
        FirebaseAuthUtil.registerWithProfile(
                userProfile,
                password,
                this,
                new FirebaseAuthUtil.AuthResultCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                FirebaseAuthUtil.logout();
                Toast.makeText(RegistroActivity.this, R.string.msg_account_created_success, Toast.LENGTH_SHORT).show();
                navigateToLogin();
                finish();
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                setLoading(false);
                Toast.makeText(RegistroActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showError(@NonNull String message, @Nullable ValidationUtils.Field field) {
        if (field == null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            return;
        }

        if (field == ValidationUtils.Field.NAME) {
            edtNombreRegistro.setError(message);
            edtNombreRegistro.requestFocus();
            return;
        }

        if (field == ValidationUtils.Field.ALIAS) {
            edtAliasRegistro.setError(message);
            edtAliasRegistro.requestFocus();
            return;
        }

        if (field == ValidationUtils.Field.EMAIL) {
            edtCorreoRegistro.setError(message);
            edtCorreoRegistro.requestFocus();
            return;
        }

        if (field == ValidationUtils.Field.PASSWORD) {
            edtPasswordRegistro.setError(message);
            edtPasswordRegistro.requestFocus();
            return;
        }

        if (field == ValidationUtils.Field.CONFIRM_PASSWORD) {
            edtConfirmarPasswordRegistro.setError(message);
            edtConfirmarPasswordRegistro.requestFocus();
            return;
        }

        if (field == ValidationUtils.Field.CLUB_CODE) {
            edtCodigoClub.setError(message);
            edtCodigoClub.requestFocus();
            return;
        }

        if (field == ValidationUtils.Field.NIVEL_PADEL) {
            actvNivelPadel.setError(message);
            actvNivelPadel.requestFocus();
            return;
        }

        if (field == ValidationUtils.Field.NIVEL_TENIS) {
            actvNivelTenis.setError(message);
            actvNivelTenis.requestFocus();
            return;
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void clearErrors() {
        edtNombreRegistro.setError(null);
        edtAliasRegistro.setError(null);
        edtCorreoRegistro.setError(null);
        edtPasswordRegistro.setError(null);
        edtConfirmarPasswordRegistro.setError(null);
        edtCodigoClub.setError(null);
        actvNivelPadel.setError(null);
        actvNivelTenis.setError(null);
    }

    private void setLoading(boolean isLoading) {
        progressRegistro.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnCrearCuenta.setEnabled(!isLoading);
        btnSelectorUser.setEnabled(!isLoading);
        btnSelectorClub.setEnabled(!isLoading);
        txtIrLogin.setEnabled(!isLoading);
        edtNombreRegistro.setEnabled(!isLoading);
        edtAliasRegistro.setEnabled(!isLoading);
        edtCorreoRegistro.setEnabled(!isLoading);
        edtPasswordRegistro.setEnabled(!isLoading);
        edtConfirmarPasswordRegistro.setEnabled(!isLoading);
        edtCodigoClub.setEnabled(!isLoading);
        actvNivelPadel.setEnabled(!isLoading);
        actvNivelTenis.setEnabled(!isLoading);
    }

    @NonNull
    private String textOf(@NonNull EditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }

    private void selectRole(@NonNull String role) {
        selectedRole = role;
        boolean isClub = ValidationUtils.ROLE_CLUB.equals(role);

        layoutCodigoClub.setVisibility(isClub ? View.VISIBLE : View.GONE);
        if (!isClub) {
            edtCodigoClub.setText("");
            edtCodigoClub.setError(null);
        }

        btnSelectorUser.setBackgroundResource(
                isClub ? R.drawable.bg_boton_secundario : R.drawable.bg_boton_primario
        );
        btnSelectorClub.setBackgroundResource(
                isClub ? R.drawable.bg_boton_primario : R.drawable.bg_boton_secundario
        );
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
