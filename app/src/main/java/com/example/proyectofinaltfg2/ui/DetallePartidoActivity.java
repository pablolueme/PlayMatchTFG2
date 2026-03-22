package com.example.proyectofinaltfg2.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.logic.PartidoFechaHoraUtils;
import com.example.proyectofinaltfg2.model.Partido;
import com.example.proyectofinaltfg2.repository.PartidoRepository;
import com.example.proyectofinaltfg2.service.AuthService;

import java.util.List;
import java.util.Locale;

public class DetallePartidoActivity extends AppCompatActivity {

    public static final String EXTRA_PARTIDO_ID = "extra_partido_id";

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalle_partido_layout);

        partidoRepository = new PartidoRepository();
        authService = new AuthService();
        partidoId = extraerPartidoId();
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
        containerParticipantes = findViewById(R.id.container_participantes_detalle_partido);
        btnVolverAtras = findViewById(R.id.btn_volver_detalle_partido);
        btnApuntarse = findViewById(R.id.btn_apuntarse_detalle_partido);
    }

    private void prepararBotonVolverAtras() {
        btnVolverAtras.setOnClickListener(v -> finish());
    }

    private void prepararBotonApuntarse() {
        btnApuntarse.setText(R.string.action_unirse);
        btnApuntarse.setVisibility(android.view.View.VISIBLE);
        btnApuntarse.setEnabled(false);
        btnApuntarse.setOnClickListener(v -> gestionarClickInscripcion());
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
        actualizarBotonInscripcion(partido);
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
                        actualizarBotonInscripcion(partidoActual);
                        Toast.makeText(DetallePartidoActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                };

        if (usuarioApuntado) {
            partidoRepository.desapuntarseDePartido(this, partidoId, callback);
            return;
        }
        partidoRepository.unirseAPartido(this, partidoId, callback);
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
}
