package com.example.proyectofinaltfg2.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.logic.PartidoFechaHoraUtils;
import com.example.proyectofinaltfg2.model.Partido;
import com.example.proyectofinaltfg2.model.Resultado;
import com.example.proyectofinaltfg2.repository.PartidoRepository;
import com.example.proyectofinaltfg2.service.AuthService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class DetallePartidoActivity extends AppCompatActivity {

    public static final String EXTRA_PARTIDO_ID = "extra_partido_id";
    public static final String EXTRA_MODO_HISTORIAL = "extra_modo_historial";

    private static final String OPCION_NO_SELECCIONADA = "-";
    private static final List<String> OPCIONES_SET_VALIDAS = Arrays.asList(
            "6-0", "6-1", "6-2", "6-3", "6-4", "7-5", "7-6",
            "0-6", "1-6", "2-6", "3-6", "4-6", "5-7", "6-7"
    );

    private TextView txtBadgePlazas;
    private TextView txtTitulo;
    private TextView txtDeporte;
    private TextView txtNivel;
    private TextView txtFecha;
    private TextView txtHora;
    private TextView txtUbicacion;
    private TextView txtCreador;
    private TextView txtPlazas;
    private TextView txtEstado;
    private TextView txtLabelResultado;
    private TextView txtResultado;
    private LinearLayout containerParticipantes;
    private ImageButton btnVolverAtras;
    private Button btnApuntarse;

    private PartidoRepository partidoRepository;
    private AuthService authService;
    @Nullable
    private Partido partidoActual;
    @NonNull
    private String partidoId = "";
    @NonNull
    private String usuarioIdActual = "";
    private boolean operacionEnCurso = false;
    private boolean modoHistorial = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalle_partido_layout);

        partidoRepository = new PartidoRepository();
        authService = new AuthService();
        partidoId = extraerPartidoId();
        modoHistorial = getIntent().getBooleanExtra(EXTRA_MODO_HISTORIAL, false);
        usuarioIdActual = authService.getUsuarioIdActual();
        inicializarVistas();
        prepararBotonVolverAtras();
        prepararBotonApuntarse();
        cargarDetallePartido();
    }

    private void inicializarVistas() {
        txtBadgePlazas = findViewById(R.id.txt_badge_plazas_detalle_partido);
        txtTitulo = findViewById(R.id.txt_titulo_detalle_partido);
        txtDeporte = findViewById(R.id.txt_deporte_detalle_partido);
        txtNivel = findViewById(R.id.txt_nivel_detalle_partido);
        txtFecha = findViewById(R.id.txt_fecha_detalle_partido);
        txtHora = findViewById(R.id.txt_hora_detalle_partido);
        txtUbicacion = findViewById(R.id.txt_ubicacion_detalle_partido);
        txtCreador = findViewById(R.id.txt_creador_detalle_partido);
        txtPlazas = findViewById(R.id.txt_plazas_detalle_partido);
        txtEstado = findViewById(R.id.txt_estado_detalle_partido);
        txtLabelResultado = findViewById(R.id.txt_label_resultado_detalle_partido);
        txtResultado = findViewById(R.id.txt_resultado_detalle_partido);
        containerParticipantes = findViewById(R.id.container_participantes_detalle_partido);
        btnVolverAtras = findViewById(R.id.btn_volver_detalle_partido);
        btnApuntarse = findViewById(R.id.btn_apuntarse_detalle_partido);
    }

    private void prepararBotonVolverAtras() {
        btnVolverAtras.setOnClickListener(v -> finish());
    }

    private void prepararBotonApuntarse() {
        btnApuntarse.setText(modoHistorial ? R.string.historial_confirmar_resultado : R.string.action_unirse);
        btnApuntarse.setVisibility(View.VISIBLE);
        btnApuntarse.setEnabled(false);
        btnApuntarse.setOnClickListener(v -> {
            if (modoHistorial) {
                gestionarClickConfirmarResultado();
                return;
            }
            gestionarClickInscripcion();
        });
    }

    private void cargarDetallePartido() {
        if (TextUtils.isEmpty(partidoId)) {
            Toast.makeText(this, R.string.msg_partido_id_invalido, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnApuntarse.setEnabled(false);
        partidoRepository.obtenerPartidoPorId(
                this,
                partidoId,
                new PartidoRepository.ObtenerPartidoCallback() {
                    @Override
                    public void onSuccess(@NonNull Partido partido) {
                        partidoActual = partido;
                        usuarioIdActual = authService.getUsuarioIdActual();
                        mostrarPartido(partido);
                    }

                    @Override
                    public void onError(@NonNull String errorMessage) {
                        Toast.makeText(DetallePartidoActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
        );
    }

    private void mostrarPartido(@NonNull Partido partido) {
        txtTitulo.setText(getString(R.string.partido_titulo_item_format, capitalizar(partido.getDeporte())));
        txtDeporte.setText(capitalizar(partido.getDeporte()));
        txtNivel.setText(partido.getNivel());
        txtFecha.setText(PartidoFechaHoraUtils.normalizarFecha(partido.getFecha()));
        txtHora.setText(PartidoFechaHoraUtils.normalizarHora(partido.getHora()));
        txtUbicacion.setText(partido.getDireccion());
        txtCreador.setText(partido.getCreadorNombre());
        txtPlazas.setText(
                getString(
                        R.string.partido_plazas_ocupadas_format,
                        partido.getPlazasOcupadas(),
                        partido.getMaxJugadores()
                )
        );
        txtEstado.setText(partido.getEstado());
        txtBadgePlazas.setText(getString(R.string.partido_plazas_libres_format, partido.getPlazasLibres()));
        mostrarParticipantes(partido);
        mostrarResultado(partido);
        actualizarAccionPrincipal(partido);
    }

    private void mostrarResultado(@NonNull Partido partido) {
        if (!partido.tieneResultadoConfirmado()
                || partido.getResultado() == null
                || TextUtils.isEmpty(partido.getResultado().getMarcador())) {
            txtLabelResultado.setVisibility(View.GONE);
            txtResultado.setVisibility(View.GONE);
            txtResultado.setText("");
            return;
        }

        txtLabelResultado.setVisibility(View.VISIBLE);
        txtResultado.setVisibility(View.VISIBLE);
        txtResultado.setText(partido.getResultado().getMarcador());
    }

    private void mostrarParticipantes(@NonNull Partido partido) {
        containerParticipantes.removeAllViews();

        List<String> participantes = partido.getParticipantes();
        String nombreCreador = partido.getCreadorNombre();
        if (TextUtils.isEmpty(nombreCreador)) {
            nombreCreador = getString(R.string.home_nombre_no_disponible);
        }

        for (String participanteId : participantes) {
            if (TextUtils.isEmpty(participanteId)) {
                continue;
            }
            if (participanteId.equals(partido.getCreadorId())) {
                agregarParticipanteVisual(
                        getString(R.string.detalle_partido_participante_creador, nombreCreador),
                        true
                );
                continue;
            }
            if (!TextUtils.isEmpty(usuarioIdActual) && participanteId.equals(usuarioIdActual)) {
                agregarParticipanteVisual(
                        getString(R.string.detalle_partido_participante_actual),
                        false
                );
                continue;
            }

            agregarParticipanteVisual(
                    getString(R.string.detalle_partido_participante_generico),
                    false
            );
        }

        int plazasSinId = Math.max(partido.getPlazasOcupadas() - participantes.size(), 0);
        int acompanantesIniciales = Math.max(partido.getAcompanantesIniciales(), 0);
        for (int i = 0; i < plazasSinId; i++) {
            if (i < acompanantesIniciales) {
                agregarParticipanteVisual(
                        getString(R.string.detalle_partido_participante_acompanante),
                        false
                );
                continue;
            }
            agregarParticipanteVisual(
                    getString(R.string.detalle_partido_participante_generico),
                    false
            );
        }

        if (containerParticipantes.getChildCount() == 0) {
            agregarParticipanteVisual(
                    getString(R.string.detalle_partido_sin_participantes),
                    false
            );
        }
    }

    private void gestionarClickInscripcion() {
        if (partidoActual == null || operacionEnCurso) {
            return;
        }

        if (!authService.isUsuarioAutenticado()) {
            Toast.makeText(this, R.string.msg_auth_invalid_credentials, Toast.LENGTH_SHORT).show();
            return;
        }

        usuarioIdActual = authService.getUsuarioIdActual();
        if (TextUtils.isEmpty(usuarioIdActual)) {
            Toast.makeText(this, R.string.msg_auth_invalid_credentials, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!esPartidoAbierto(partidoActual.getEstado())) {
            Toast.makeText(this, R.string.msg_partido_operacion_error, Toast.LENGTH_SHORT).show();
            return;
        }

        boolean usuarioApuntado = estaUsuarioApuntado(partidoActual);
        boolean partidoCompleto = partidoActual.getMaxJugadores() > 0
                && partidoActual.getPlazasOcupadas() >= partidoActual.getMaxJugadores();
        if (!usuarioApuntado && partidoCompleto) {
            Toast.makeText(this, R.string.msg_partido_sin_plazas, Toast.LENGTH_SHORT).show();
            return;
        }

        operacionEnCurso = true;
        btnApuntarse.setEnabled(false);

        PartidoRepository.GestionInscripcionCallback callback =
                new PartidoRepository.GestionInscripcionCallback() {
                    @Override
                    public void onSuccess(@NonNull Partido partidoActualizado, @NonNull String mensajeExito) {
                        operacionEnCurso = false;
                        partidoActual = partidoActualizado;
                        mostrarPartido(partidoActualizado);
                        Toast.makeText(DetallePartidoActivity.this, mensajeExito, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull String errorMessage) {
                        operacionEnCurso = false;
                        actualizarAccionPrincipal(partidoActual);
                        Toast.makeText(DetallePartidoActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                };

        if (usuarioApuntado) {
            partidoRepository.desapuntarseDePartido(this, partidoId, callback);
            return;
        }
        partidoRepository.unirseAPartido(this, partidoId, callback);
    }

    private void gestionarClickConfirmarResultado() {
        if (partidoActual == null || operacionEnCurso) {
            return;
        }

        if (!authService.isUsuarioAutenticado()) {
            Toast.makeText(this, R.string.msg_auth_invalid_credentials, Toast.LENGTH_SHORT).show();
            return;
        }

        usuarioIdActual = authService.getUsuarioIdActual();
        if (TextUtils.isEmpty(usuarioIdActual)) {
            Toast.makeText(this, R.string.msg_auth_invalid_credentials, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!partidoActual.participaUsuario(usuarioIdActual)) {
            Toast.makeText(this, R.string.msg_resultado_solo_participantes, Toast.LENGTH_SHORT).show();
            return;
        }
        if (partidoActual.tieneResultadoConfirmado()) {
            Toast.makeText(this, R.string.msg_resultado_ya_confirmado, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!puedeConfirmarseResultado(partidoActual)) {
            Toast.makeText(this, R.string.msg_resultado_partido_no_jugado, Toast.LENGTH_SHORT).show();
            return;
        }

        mostrarDialogoConfirmarResultado();
    }

    private void mostrarDialogoConfirmarResultado() {
        View vistaDialogo = getLayoutInflater().inflate(
                R.layout.dialog_confirmar_resultado,
                null,
                false
        );

        Spinner spinnerSet1 = vistaDialogo.findViewById(R.id.spinner_set1_resultado);
        Spinner spinnerSet2 = vistaDialogo.findViewById(R.id.spinner_set2_resultado);
        Spinner spinnerSet3 = vistaDialogo.findViewById(R.id.spinner_set3_resultado);

        LinearLayout containerSet3 = vistaDialogo.findViewById(R.id.container_set3_resultado);
        LinearLayout containerTieBreakSet1 = vistaDialogo.findViewById(R.id.container_tiebreak_set1_resultado);
        LinearLayout containerTieBreakSet2 = vistaDialogo.findViewById(R.id.container_tiebreak_set2_resultado);
        LinearLayout containerTieBreakSet3 = vistaDialogo.findViewById(R.id.container_tiebreak_set3_resultado);

        Spinner spinnerTieBreakSet1Local = vistaDialogo.findViewById(R.id.spinner_tiebreak_set1_local);
        Spinner spinnerTieBreakSet1Visitante = vistaDialogo.findViewById(R.id.spinner_tiebreak_set1_visitante);
        Spinner spinnerTieBreakSet2Local = vistaDialogo.findViewById(R.id.spinner_tiebreak_set2_local);
        Spinner spinnerTieBreakSet2Visitante = vistaDialogo.findViewById(R.id.spinner_tiebreak_set2_visitante);
        Spinner spinnerTieBreakSet3Local = vistaDialogo.findViewById(R.id.spinner_tiebreak_set3_local);
        Spinner spinnerTieBreakSet3Visitante = vistaDialogo.findViewById(R.id.spinner_tiebreak_set3_visitante);

        configurarSpinnerOpciones(spinnerSet1, obtenerOpcionesSet());
        configurarSpinnerOpciones(spinnerSet2, obtenerOpcionesSet());
        configurarSpinnerOpciones(spinnerSet3, obtenerOpcionesSet());
        List<String> opcionesTieBreak = obtenerOpcionesTieBreak();
        configurarSpinnerOpciones(spinnerTieBreakSet1Local, opcionesTieBreak);
        configurarSpinnerOpciones(spinnerTieBreakSet1Visitante, opcionesTieBreak);
        configurarSpinnerOpciones(spinnerTieBreakSet2Local, opcionesTieBreak);
        configurarSpinnerOpciones(spinnerTieBreakSet2Visitante, opcionesTieBreak);
        configurarSpinnerOpciones(spinnerTieBreakSet3Local, opcionesTieBreak);
        configurarSpinnerOpciones(spinnerTieBreakSet3Visitante, opcionesTieBreak);

        Runnable actualizarDialogo = () -> actualizarDialogoResultado(
                spinnerSet1,
                spinnerSet2,
                spinnerSet3,
                containerSet3,
                containerTieBreakSet1,
                containerTieBreakSet2,
                containerTieBreakSet3
        );

        spinnerSet1.setOnItemSelectedListener(new SimpleItemSelectedListener(actualizarDialogo));
        spinnerSet2.setOnItemSelectedListener(new SimpleItemSelectedListener(actualizarDialogo));
        spinnerSet3.setOnItemSelectedListener(new SimpleItemSelectedListener(actualizarDialogo));
        actualizarDialogo.run();

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.confirmar_resultado_titulo_dialogo)
                .setView(vistaDialogo)
                .setNegativeButton(R.string.action_cancelar, null)
                .setPositiveButton(R.string.action_confirmar, null)
                .create();

        dialog.setOnShowListener(unused -> {
            Button btnConfirmar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnConfirmar.setOnClickListener(v -> {
                Resultado resultado = construirResultadoDesdeDialogo(
                        spinnerSet1,
                        spinnerSet2,
                        spinnerSet3,
                        spinnerTieBreakSet1Local,
                        spinnerTieBreakSet1Visitante,
                        spinnerTieBreakSet2Local,
                        spinnerTieBreakSet2Visitante,
                        spinnerTieBreakSet3Local,
                        spinnerTieBreakSet3Visitante
                );
                if (resultado == null) {
                    return;
                }
                dialog.dismiss();
                confirmarResultadoPartido(resultado);
            });
        });
        dialog.show();
    }

    @Nullable
    private Resultado construirResultadoDesdeDialogo(
            @NonNull Spinner spinnerSet1,
            @NonNull Spinner spinnerSet2,
            @NonNull Spinner spinnerSet3,
            @NonNull Spinner spinnerTieBreakSet1Local,
            @NonNull Spinner spinnerTieBreakSet1Visitante,
            @NonNull Spinner spinnerTieBreakSet2Local,
            @NonNull Spinner spinnerTieBreakSet2Visitante,
            @NonNull Spinner spinnerTieBreakSet3Local,
            @NonNull Spinner spinnerTieBreakSet3Visitante
    ) {
        String set1 = obtenerValorSpinner(spinnerSet1);
        String set2 = obtenerValorSpinner(spinnerSet2);

        if (set1.isEmpty() || set2.isEmpty()) {
            Toast.makeText(this, R.string.msg_resultado_set_obligatorio, Toast.LENGTH_SHORT).show();
            return null;
        }

        Resultado resultado = new Resultado();
        resultado.setSet1(set1);
        resultado.setSet2(set2);
        resultado.setTieBreakSet1(obtenerTieBreakSeleccionado(set1, spinnerTieBreakSet1Local, spinnerTieBreakSet1Visitante));
        resultado.setTieBreakSet2(obtenerTieBreakSeleccionado(set2, spinnerTieBreakSet2Local, spinnerTieBreakSet2Visitante));

        if (requiereSet3(set1, set2)) {
            String set3 = obtenerValorSpinner(spinnerSet3);
            if (set3.isEmpty()) {
                Toast.makeText(this, R.string.msg_resultado_set3_obligatorio, Toast.LENGTH_SHORT).show();
                return null;
            }
            resultado.setSet3(set3);
            resultado.setTieBreakSet3(obtenerTieBreakSeleccionado(set3, spinnerTieBreakSet3Local, spinnerTieBreakSet3Visitante));
        } else {
            resultado.setSet3("");
            resultado.setTieBreakSet3("");
        }

        return resultado;
    }

    private void confirmarResultadoPartido(@NonNull Resultado resultado) {
        operacionEnCurso = true;
        actualizarAccionPrincipal(partidoActual);

        partidoRepository.confirmarResultado(
                this,
                partidoId,
                resultado,
                new PartidoRepository.ConfirmarResultadoCallback() {
                    @Override
                    public void onSuccess(@NonNull Partido partidoActualizado, @NonNull String mensajeExito) {
                        operacionEnCurso = false;
                        partidoActual = partidoActualizado;
                        mostrarPartido(partidoActualizado);
                        Toast.makeText(DetallePartidoActivity.this, mensajeExito, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull String errorMessage) {
                        operacionEnCurso = false;
                        actualizarAccionPrincipal(partidoActual);
                        Toast.makeText(DetallePartidoActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void actualizarDialogoResultado(
            @NonNull Spinner spinnerSet1,
            @NonNull Spinner spinnerSet2,
            @NonNull Spinner spinnerSet3,
            @NonNull LinearLayout containerSet3,
            @NonNull LinearLayout containerTieBreakSet1,
            @NonNull LinearLayout containerTieBreakSet2,
            @NonNull LinearLayout containerTieBreakSet3
    ) {
        String set1 = obtenerValorSpinner(spinnerSet1);
        String set2 = obtenerValorSpinner(spinnerSet2);
        String set3 = obtenerValorSpinner(spinnerSet3);

        containerTieBreakSet1.setVisibility(requiereTieBreak(set1) ? View.VISIBLE : View.GONE);
        containerTieBreakSet2.setVisibility(requiereTieBreak(set2) ? View.VISIBLE : View.GONE);

        boolean mostrarSet3 = requiereSet3(set1, set2);
        containerSet3.setVisibility(mostrarSet3 ? View.VISIBLE : View.GONE);
        if (!mostrarSet3) {
            if (spinnerSet3.getSelectedItemPosition() != 0) {
                spinnerSet3.setSelection(0);
            }
            containerTieBreakSet3.setVisibility(View.GONE);
            return;
        }
        containerTieBreakSet3.setVisibility(requiereTieBreak(set3) ? View.VISIBLE : View.GONE);
    }

    private void configurarSpinnerOpciones(@NonNull Spinner spinner, @NonNull List<String> opciones) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                opciones
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
    }

    @NonNull
    private List<String> obtenerOpcionesSet() {
        List<String> opciones = new ArrayList<>();
        opciones.add(OPCION_NO_SELECCIONADA);
        opciones.addAll(OPCIONES_SET_VALIDAS);
        return opciones;
    }

    @NonNull
    private List<String> obtenerOpcionesTieBreak() {
        List<String> opciones = new ArrayList<>();
        opciones.add(OPCION_NO_SELECCIONADA);
        for (int i = 0; i <= 20; i++) {
            opciones.add(String.valueOf(i));
        }
        return opciones;
    }

    @NonNull
    private String obtenerTieBreakSeleccionado(
            @NonNull String set,
            @NonNull Spinner spinnerLocal,
            @NonNull Spinner spinnerVisitante
    ) {
        if (!requiereTieBreak(set)) {
            return "";
        }

        String puntosLocal = obtenerValorSpinner(spinnerLocal);
        String puntosVisitante = obtenerValorSpinner(spinnerVisitante);
        if (puntosLocal.isEmpty() || puntosVisitante.isEmpty()) {
            return "";
        }
        return puntosLocal + "-" + puntosVisitante;
    }

    @NonNull
    private String obtenerValorSpinner(@NonNull Spinner spinner) {
        Object selected = spinner.getSelectedItem();
        if (selected == null) {
            return "";
        }
        String valor = String.valueOf(selected).trim();
        if (valor.equals(OPCION_NO_SELECCIONADA)) {
            return "";
        }
        return valor;
    }

    private boolean requiereSet3(@NonNull String set1, @NonNull String set2) {
        int ganadorSet1 = obtenerGanadorSet(set1);
        int ganadorSet2 = obtenerGanadorSet(set2);
        if (ganadorSet1 == 0 || ganadorSet2 == 0) {
            return false;
        }
        return ganadorSet1 != ganadorSet2;
    }

    private int obtenerGanadorSet(@Nullable String set) {
        if (set == null || set.trim().isEmpty() || !set.contains("-")) {
            return 0;
        }
        String[] partes = set.split("-");
        if (partes.length != 2) {
            return 0;
        }
        try {
            int local = Integer.parseInt(partes[0].trim());
            int visitante = Integer.parseInt(partes[1].trim());
            if (local > visitante) {
                return 1;
            }
            if (visitante > local) {
                return 2;
            }
            return 0;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private boolean requiereTieBreak(@Nullable String set) {
        if (set == null) {
            return false;
        }
        String setNormalizado = set.trim();
        return "7-6".equals(setNormalizado) || "6-7".equals(setNormalizado);
    }

    private void actualizarAccionPrincipal(@Nullable Partido partido) {
        if (modoHistorial) {
            actualizarBotonHistorial(partido);
            return;
        }
        actualizarBotonInscripcion(partido);
    }

    private void actualizarBotonHistorial(@Nullable Partido partido) {
        if (partido == null) {
            btnApuntarse.setText(R.string.historial_confirmar_resultado);
            btnApuntarse.setEnabled(false);
            btnApuntarse.setVisibility(View.VISIBLE);
            btnApuntarse.setAlpha(1f);
            return;
        }

        if (partido.tieneResultadoConfirmado()) {
            btnApuntarse.setVisibility(View.GONE);
            return;
        }

        if (!puedeConfirmarseResultado(partido)) {
            btnApuntarse.setVisibility(View.GONE);
            return;
        }

        btnApuntarse.setVisibility(View.VISIBLE);
        btnApuntarse.setText(R.string.historial_confirmar_resultado);
        btnApuntarse.setEnabled(!operacionEnCurso);
        btnApuntarse.setAlpha(1f);
    }

    private boolean puedeConfirmarseResultado(@NonNull Partido partido) {
        if (Partido.ESTADO_CANCELADO.equals(partido.getEstado())) {
            return false;
        }
        if (!partido.estaCompleto()) {
            return false;
        }
        LocalDateTime fechaHoraPartido = PartidoFechaHoraUtils.parsearFechaHora(
                partido.getFecha(),
                partido.getHora()
        );
        if (fechaHoraPartido == null) {
            return false;
        }
        return fechaHoraPartido.isBefore(LocalDateTime.now());
    }

    private void actualizarBotonInscripcion(@Nullable Partido partido) {
        if (partido == null) {
            btnApuntarse.setText(R.string.action_unirse);
            btnApuntarse.setEnabled(false);
            btnApuntarse.setAlpha(1f);
            return;
        }

        boolean autenticado = authService.isUsuarioAutenticado() && !TextUtils.isEmpty(usuarioIdActual);
        boolean partidoAbierto = esPartidoAbierto(partido.getEstado());
        boolean usuarioApuntado = estaUsuarioApuntado(partido);
        boolean partidoCompleto = partido.getMaxJugadores() > 0
                && partido.getPlazasOcupadas() >= partido.getMaxJugadores();

        if (usuarioApuntado) {
            btnApuntarse.setText(R.string.action_desapuntarse);
            btnApuntarse.setEnabled(!operacionEnCurso && autenticado && partidoAbierto);
            btnApuntarse.setAlpha(1f);
            return;
        }

        btnApuntarse.setText(R.string.action_unirse);
        btnApuntarse.setEnabled(!operacionEnCurso && autenticado && partidoAbierto && !partidoCompleto);
        btnApuntarse.setAlpha(partidoCompleto ? 0.6f : 1f);
    }

    private boolean estaUsuarioApuntado(@NonNull Partido partido) {
        if (TextUtils.isEmpty(usuarioIdActual)) {
            return false;
        }
        return partido.getParticipantes().contains(usuarioIdActual);
    }

    private boolean esPartidoAbierto(@NonNull String estado) {
        return Partido.ESTADO_ACTIVO.equals(estado) || Partido.ESTADO_COMPLETO.equals(estado);
    }

    @NonNull
    private String extraerPartidoId() {
        String partidoIdExtra = getIntent().getStringExtra(EXTRA_PARTIDO_ID);
        if (partidoIdExtra == null) {
            return "";
        }
        return partidoIdExtra.trim();
    }

    private void agregarParticipanteVisual(@NonNull String texto, boolean esCreador) {
        TextView participanteView = new TextView(this);
        participanteView.setText(texto);
        participanteView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        participanteView.setTextColor(ContextCompat.getColor(
                this,
                esCreador ? R.color.chip_padel_text : R.color.warning_text
        ));
        participanteView.setBackgroundResource(esCreador ? R.drawable.bg_chip_padel : R.drawable.bg_chip_pendiente);
        int paddingHorizontal = getResources().getDimensionPixelSize(R.dimen.spacing_12);
        int paddingVertical = getResources().getDimensionPixelSize(R.dimen.spacing_8);
        participanteView.setPadding(
                paddingHorizontal,
                paddingVertical,
                paddingHorizontal,
                paddingVertical
        );

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.spacing_8);
        participanteView.setLayoutParams(params);
        containerParticipantes.addView(participanteView);
    }

    @NonNull
    private String capitalizar(@Nullable String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return "";
        }
        String valor = texto.trim();
        return valor.substring(0, 1).toUpperCase(Locale.getDefault())
                + valor.substring(1).toLowerCase(Locale.getDefault());
    }

    private static class SimpleItemSelectedListener implements android.widget.AdapterView.OnItemSelectedListener {

        @NonNull
        private final Runnable onChange;

        SimpleItemSelectedListener(@NonNull Runnable onChange) {
            this.onChange = onChange;
        }

        @Override
        public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
            onChange.run();
        }

        @Override
        public void onNothingSelected(android.widget.AdapterView<?> parent) {
            onChange.run();
        }
    }
}
