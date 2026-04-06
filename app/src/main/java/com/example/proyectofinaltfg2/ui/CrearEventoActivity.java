package com.example.proyectofinaltfg2.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.logic.PartidoFechaHoraUtils;
import com.example.proyectofinaltfg2.model.Evento;
import com.example.proyectofinaltfg2.model.UserProfile;
import com.example.proyectofinaltfg2.repository.EventoRepository;
import com.example.proyectofinaltfg2.repository.UsuarioRepository;
import com.example.proyectofinaltfg2.utils.ValidationUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CrearEventoActivity extends AppCompatActivity {

    private static final DateTimeFormatter FORMATO_FECHA_SALIDA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
    private static final DateTimeFormatter FORMATO_HORA_SALIDA =
            DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

    private ImageButton btnVolver;
    private EditText edtTitulo;
    private EditText edtDescripcion;
    private EditText edtFecha;
    private EditText edtHora;
    private EditText edtUbicacion;
    private Button btnPublicarEvento;
    private ProgressBar progressCrearEvento;

    private EventoRepository eventoRepository;
    private UsuarioRepository usuarioRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crear_evento_layout);

        eventoRepository = new EventoRepository();
        usuarioRepository = new UsuarioRepository();

        inicializarVistas();
        configurarEventos();
        validarRolClub();
    }

    private void inicializarVistas() {
        btnVolver = findViewById(R.id.btn_volver_crear_evento);
        edtTitulo = findViewById(R.id.edt_titulo_crear_evento);
        edtDescripcion = findViewById(R.id.edt_descripcion_crear_evento);
        edtFecha = findViewById(R.id.edt_fecha_crear_evento);
        edtHora = findViewById(R.id.edt_hora_crear_evento);
        edtUbicacion = findViewById(R.id.edt_ubicacion_crear_evento);
        btnPublicarEvento = findViewById(R.id.btn_publicar_evento);
        progressCrearEvento = findViewById(R.id.progress_crear_evento);
    }

    private void configurarEventos() {
        btnVolver.setOnClickListener(v -> finish());
        configurarCampoSoloSelector(edtFecha, this::mostrarSelectorFecha);
        configurarCampoSoloSelector(edtHora, this::mostrarSelectorHora);
        btnPublicarEvento.setOnClickListener(v -> intentarPublicarEvento());
    }

    private void configurarCampoSoloSelector(
            @NonNull EditText campo,
            @NonNull Runnable accionMostrarSelector
    ) {
        campo.setShowSoftInputOnFocus(false);
        campo.setFocusable(false);
        campo.setFocusableInTouchMode(false);
        campo.setOnClickListener(v -> accionMostrarSelector.run());
    }

    private void validarRolClub() {
        usuarioRepository.obtenerUsuarioActual(this, new UsuarioRepository.UsuarioResultCallback() {
            @Override
            public void onSuccess(@NonNull UserProfile userProfile) {
                if (ValidationUtils.ROLE_CLUB.equals(userProfile.getRol())) {
                    return;
                }
                Toast.makeText(CrearEventoActivity.this, R.string.msg_eventos_solo_club_crear, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Toast.makeText(CrearEventoActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
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

    private void intentarPublicarEvento() {
        limpiarErrores();

        String titulo = obtenerTexto(edtTitulo);
        String descripcion = obtenerTexto(edtDescripcion);
        String fecha = obtenerTexto(edtFecha);
        String hora = obtenerTexto(edtHora);
        String ubicacion = obtenerTexto(edtUbicacion);

        if (titulo.isEmpty()) {
            edtTitulo.setError(getString(R.string.msg_evento_campos_obligatorios));
            edtTitulo.requestFocus();
            return;
        }
        if (descripcion.isEmpty()) {
            edtDescripcion.setError(getString(R.string.msg_evento_campos_obligatorios));
            edtDescripcion.requestFocus();
            return;
        }
        if (fecha.isEmpty()) {
            edtFecha.setError(getString(R.string.msg_evento_campos_obligatorios));
            edtFecha.requestFocus();
            return;
        }
        if (hora.isEmpty()) {
            edtHora.setError(getString(R.string.msg_evento_campos_obligatorios));
            edtHora.requestFocus();
            return;
        }
        if (ubicacion.isEmpty()) {
            edtUbicacion.setError(getString(R.string.msg_evento_campos_obligatorios));
            edtUbicacion.requestFocus();
            return;
        }
        if (PartidoFechaHoraUtils.parsearFecha(fecha) == null) {
            edtFecha.setError(getString(R.string.msg_evento_fecha_invalida));
            edtFecha.requestFocus();
            return;
        }
        if (PartidoFechaHoraUtils.parsearHora(hora) == null) {
            edtHora.setError(getString(R.string.msg_evento_hora_invalida));
            edtHora.requestFocus();
            return;
        }

        mostrarCarga(true);
        eventoRepository.crearEvento(
                this,
                titulo,
                descripcion,
                fecha,
                hora,
                ubicacion,
                new EventoRepository.CrearEventoCallback() {
                    @Override
                    public void onSuccess(@NonNull Evento evento) {
                        mostrarCarga(false);
                        Toast.makeText(CrearEventoActivity.this, R.string.msg_evento_publicado_ok, Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(@NonNull String errorMessage) {
                        mostrarCarga(false);
                        Toast.makeText(CrearEventoActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void limpiarErrores() {
        edtTitulo.setError(null);
        edtDescripcion.setError(null);
        edtFecha.setError(null);
        edtHora.setError(null);
        edtUbicacion.setError(null);
    }

    private void mostrarCarga(boolean cargando) {
        progressCrearEvento.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnVolver.setEnabled(!cargando);
        edtTitulo.setEnabled(!cargando);
        edtDescripcion.setEnabled(!cargando);
        edtFecha.setEnabled(!cargando);
        edtHora.setEnabled(!cargando);
        edtUbicacion.setEnabled(!cargando);
        btnPublicarEvento.setEnabled(!cargando);
    }

    @NonNull
    private String obtenerTexto(@NonNull EditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}
