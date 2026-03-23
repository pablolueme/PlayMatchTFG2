package com.example.proyectofinaltfg2.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.logic.ValidadorPartido;
import com.example.proyectofinaltfg2.model.Partido;
import com.example.proyectofinaltfg2.model.UserProfile;
import com.example.proyectofinaltfg2.repository.PartidoRepository;
import com.example.proyectofinaltfg2.repository.UsuarioRepository;
import com.example.proyectofinaltfg2.utils.ValidationUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CrearPartidoActivity extends AppCompatActivity {

    private static final DateTimeFormatter FORMATO_FECHA_SALIDA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
    private static final DateTimeFormatter FORMATO_HORA_SALIDA =
            DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());
    private static final List<String> OPCIONES_NIVEL = Arrays.asList("1", "2", "3", "4", "5");
    private static final List<String> OPCIONES_PERSONAS_INICIALES_PADEL = Arrays.asList("1", "2");

    private Button btnSelectorPadel;
    private Button btnSelectorTenis;
    private ImageButton btnVolver;
    private EditText edtFecha;
    private EditText edtHora;
    private EditText edtDireccion;
    private Spinner spinnerNivel;
    private Button btnCrearPartido;
    private LinearLayout containerPersonasInicialesPadel;
    private Spinner spinnerPersonasInicialesPadel;

    private PartidoRepository partidoRepository;
    private UsuarioRepository usuarioRepository;
    private String deporteSeleccionado = Partido.DEPORTE_PADEL;
    @Nullable
    private UserProfile usuarioActual;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crear_partido_layout);

        partidoRepository = new PartidoRepository();
        usuarioRepository = new UsuarioRepository();

        inicializarVistas();
        configurarSpinners();
        configurarEventos();
        actualizarEstiloSelectorDeporte();
        actualizarVisibilidadPersonasIniciales();
        cargarPerfilActual();
    }

    private void inicializarVistas() {
        btnSelectorPadel = findViewById(R.id.btn_selector_padel_crear_partido);
        btnSelectorTenis = findViewById(R.id.btn_selector_tenis_crear_partido);
        btnVolver = findViewById(R.id.btn_volver_crear_partido);
        edtFecha = findViewById(R.id.edt_fecha_crear_partido);
        edtHora = findViewById(R.id.edt_hora_crear_partido);
        edtDireccion = findViewById(R.id.edt_direccion_crear_partido);
        spinnerNivel = findViewById(R.id.spinner_nivel_crear_partido);
        btnCrearPartido = findViewById(R.id.btn_crear_partido);
        containerPersonasInicialesPadel = findViewById(R.id.container_personas_iniciales_padel_crear_partido);
        spinnerPersonasInicialesPadel = findViewById(R.id.spinner_personas_iniciales_padel_crear_partido);
    }

    private void configurarSpinners() {
        ArrayAdapter<String> adapterNivel = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                OPCIONES_NIVEL
        );
        adapterNivel.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNivel.setAdapter(adapterNivel);
        spinnerNivel.setSelection(0);

        ArrayAdapter<String> adapterPersonasIniciales = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                OPCIONES_PERSONAS_INICIALES_PADEL
        );
        adapterPersonasIniciales.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPersonasInicialesPadel.setAdapter(adapterPersonasIniciales);
        spinnerPersonasInicialesPadel.setSelection(0);
    }

    private void configurarEventos() {
        btnVolver.setOnClickListener(v -> finish());

        btnSelectorPadel.setOnClickListener(v -> {
            seleccionarDeporte(Partido.DEPORTE_PADEL);
        });

        btnSelectorTenis.setOnClickListener(v -> {
            seleccionarDeporte(Partido.DEPORTE_TENIS);
        });

        configurarCampoFechaHoraSoloSelector(edtFecha, this::mostrarSelectorFecha);
        configurarCampoFechaHoraSoloSelector(edtHora, this::mostrarSelectorHora);
        btnCrearPartido.setOnClickListener(v -> intentarCrearPartido());
    }

    private void configurarCampoFechaHoraSoloSelector(
            @NonNull EditText campo,
            @NonNull Runnable accionMostrarSelector
    ) {
        campo.setShowSoftInputOnFocus(false);
        campo.setFocusable(false);
        campo.setFocusableInTouchMode(false);
        campo.setOnClickListener(v -> accionMostrarSelector.run());
    }

    private void seleccionarDeporte(@NonNull String deporte) {
        deporteSeleccionado = deporte;
        actualizarEstiloSelectorDeporte();
        actualizarVisibilidadPersonasIniciales();
        precargarNivelSegunDeporte();
    }

    private void actualizarEstiloSelectorDeporte() {
        boolean padelSeleccionado = Partido.DEPORTE_PADEL.equals(deporteSeleccionado);
        aplicarEstiloBotonDeporte(btnSelectorPadel, padelSeleccionado);
        aplicarEstiloBotonDeporte(btnSelectorTenis, !padelSeleccionado);
    }

    private void aplicarEstiloBotonDeporte(@NonNull Button boton, boolean seleccionado) {
        int fondo = seleccionado ? R.drawable.bg_boton_primario : R.drawable.bg_boton_secundario;
        int colorTexto = seleccionado ? R.color.text_on_primary : R.color.primary_brand;
        boton.setBackgroundResource(fondo);
        boton.setTextColor(ContextCompat.getColor(this, colorTexto));
    }

    private void actualizarVisibilidadPersonasIniciales() {
        boolean esPadel = Partido.DEPORTE_PADEL.equals(deporteSeleccionado);
        containerPersonasInicialesPadel.setVisibility(esPadel ? View.VISIBLE : View.GONE);
        if (!esPadel) {
            spinnerPersonasInicialesPadel.setSelection(0);
        }
    }

    private void mostrarSelectorFecha() {
        LocalDate ahora = LocalDate.now();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    LocalDate fechaSeleccionada = LocalDate.of(year, month + 1, dayOfMonth);
                    edtFecha.setText(fechaSeleccionada.format(FORMATO_FECHA_SALIDA));
                },
                ahora.getYear(),
                ahora.getMonthValue() - 1,
                ahora.getDayOfMonth()
        );
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000L);
        dialog.show();
    }

    private void mostrarSelectorHora() {
        LocalTime ahora = LocalTime.now();
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    LocalTime horaSeleccionada = LocalTime.of(hourOfDay, minute);
                    edtHora.setText(horaSeleccionada.format(FORMATO_HORA_SALIDA));
                },
                ahora.getHour(),
                ahora.getMinute(),
                true
        );
        dialog.show();
    }

    private void cargarPerfilActual() {
        usuarioRepository.obtenerUsuarioActual(this, new UsuarioRepository.UsuarioResultCallback() {
            @Override
            public void onSuccess(@NonNull UserProfile userProfile) {
                usuarioActual = userProfile;
                precargarNivelSegunDeporte();
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                usuarioActual = null;
            }
        });
    }

    private void precargarNivelSegunDeporte() {
        int nivelPerfil;
        if (usuarioActual == null) {
            nivelPerfil = -1;
        } else if (Partido.DEPORTE_PADEL.equals(deporteSeleccionado)) {
            nivelPerfil = usuarioActual.getNivelPadel();
        } else {
            nivelPerfil = usuarioActual.getNivelTenis();
        }

        if (nivelPerfil < ValidationUtils.MIN_LEVEL || nivelPerfil > ValidationUtils.MAX_LEVEL) {
            spinnerNivel.setSelection(0);
            return;
        }
        seleccionarValorEnSpinner(spinnerNivel, String.valueOf(nivelPerfil));
    }

    private void seleccionarValorEnSpinner(@NonNull Spinner spinner, @NonNull String valorBuscado) {
        for (int i = 0; i < spinner.getCount(); i++) {
            Object item = spinner.getItemAtPosition(i);
            if (item == null) {
                continue;
            }
            if (valorBuscado.equals(String.valueOf(item))) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void intentarCrearPartido() {
        limpiarErrores();

        String fecha = obtenerTexto(edtFecha);
        String hora = obtenerTexto(edtHora);
        String direccion = obtenerTexto(edtDireccion);
        String nivel = obtenerNivelSeleccionado();
        int personasIniciales = obtenerPersonasIniciales();

        ValidadorPartido.ResultadoValidacion resultadoValidacion =
                ValidadorPartido.validarFormularioCrear(
                        deporteSeleccionado,
                        fecha,
                        hora,
                        direccion,
                        nivel,
                        personasIniciales
                );
        if (!resultadoValidacion.esValido()) {
            mostrarErrorValidacion(resultadoValidacion);
            return;
        }

        mostrarCarga(true);
        partidoRepository.crearPartido(
                this,
                deporteSeleccionado,
                fecha,
                hora,
                direccion,
                nivel,
                personasIniciales,
                new PartidoRepository.CrearPartidoCallback() {
                    @Override
                    public void onSuccess(@NonNull Partido partido) {
                        mostrarCarga(false);
                        Toast.makeText(
                                CrearPartidoActivity.this,
                                R.string.msg_partido_creado_ok,
                                Toast.LENGTH_SHORT
                        ).show();
                        finish();
                    }

                    @Override
                    public void onError(@NonNull String errorMessage) {
                        mostrarCarga(false);
                        Toast.makeText(CrearPartidoActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private int obtenerPersonasIniciales() {
        if (!Partido.DEPORTE_PADEL.equals(deporteSeleccionado)) {
            return 1;
        }
        Object seleccionado = spinnerPersonasInicialesPadel.getSelectedItem();
        if (seleccionado == null) {
            return 1;
        }
        try {
            return Integer.parseInt(String.valueOf(seleccionado));
        } catch (NumberFormatException ignored) {
            return 1;
        }
    }

    @NonNull
    private String obtenerNivelSeleccionado() {
        Object seleccionado = spinnerNivel.getSelectedItem();
        if (seleccionado == null) {
            return "";
        }
        return String.valueOf(seleccionado).trim();
    }

    private void mostrarErrorValidacion(@NonNull ValidadorPartido.ResultadoValidacion resultadoValidacion) {
        String mensaje = getString(resultadoValidacion.getMensajeResId());
        ValidadorPartido.Campo campo = resultadoValidacion.getCampo();
        if (campo == null || campo == ValidadorPartido.Campo.DEPORTE) {
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            return;
        }
        if (campo == ValidadorPartido.Campo.FECHA) {
            Toast.makeText(this, R.string.error_partido_fecha_hora_invalida_simple, Toast.LENGTH_SHORT).show();
            edtFecha.setError(mensaje);
            edtFecha.requestFocus();
            return;
        }
        if (campo == ValidadorPartido.Campo.HORA) {
            Toast.makeText(this, R.string.error_partido_fecha_hora_invalida_simple, Toast.LENGTH_SHORT).show();
            edtHora.setError(mensaje);
            edtHora.requestFocus();
            return;
        }
        if (campo == ValidadorPartido.Campo.DIRECCION) {
            edtDireccion.setError(mensaje);
            edtDireccion.requestFocus();
            return;
        }
        if (campo == ValidadorPartido.Campo.NIVEL) {
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            return;
        }
        if (campo == ValidadorPartido.Campo.PERSONAS_INICIALES) {
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    private void limpiarErrores() {
        edtFecha.setError(null);
        edtHora.setError(null);
        edtDireccion.setError(null);
    }

    private void mostrarCarga(boolean cargando) {
        btnCrearPartido.setEnabled(!cargando);
        btnSelectorPadel.setEnabled(!cargando);
        btnSelectorTenis.setEnabled(!cargando);
        edtFecha.setEnabled(!cargando);
        edtHora.setEnabled(!cargando);
        edtDireccion.setEnabled(!cargando);
        spinnerNivel.setEnabled(!cargando);
        spinnerPersonasInicialesPadel.setEnabled(!cargando);
    }

    @NonNull
    private String obtenerTexto(@NonNull EditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}
