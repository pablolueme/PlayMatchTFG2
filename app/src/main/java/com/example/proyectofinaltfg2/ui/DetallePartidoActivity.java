package com.example.proyectofinaltfg2.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
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
    private Button btnApuntarse;

    private PartidoRepository partidoRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalle_partido_layout);

        partidoRepository = new PartidoRepository();
        inicializarVistas();
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
        btnApuntarse = findViewById(R.id.btn_apuntarse_detalle_partido);
    }

    private void prepararBotonApuntarse() {
        btnApuntarse.setEnabled(false);
        btnApuntarse.setVisibility(View.GONE);
    }

    private void cargarDetallePartido() {
        String partidoId = getIntent().getStringExtra(EXTRA_PARTIDO_ID);
        if (partidoId == null || partidoId.trim().isEmpty()) {
            Toast.makeText(this, R.string.msg_partido_id_invalido, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        partidoRepository.obtenerPartidoPorId(
                this,
                partidoId,
                new PartidoRepository.ObtenerPartidoCallback() {
                    @Override
                    public void onSuccess(@NonNull Partido partido) {
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
    }

    private void mostrarParticipantes(@NonNull Partido partido) {
        containerParticipantes.removeAllViews();

        int plazasOcupadas = partido.getPlazasOcupadas();
        if (plazasOcupadas <= 0) {
            plazasOcupadas = 1;
        }

        String nombreCreador = partido.getCreadorNombre();
        if (TextUtils.isEmpty(nombreCreador)) {
            nombreCreador = getString(R.string.home_nombre_no_disponible);
        }
        agregarParticipanteVisual(
                getString(R.string.detalle_partido_participante_creador, nombreCreador),
                true
        );

        int participantesAdicionales = Math.max(plazasOcupadas - 1, 0);
        for (int i = 0; i < participantesAdicionales; i++) {
            if (i == 0 && partido.getAcompanantesIniciales() > 0) {
                agregarParticipanteVisual(
                        getString(R.string.detalle_partido_participante_acompanante),
                        false
                );
                continue;
            }

            agregarParticipanteVisual(
                    getString(R.string.detalle_partido_participante_pendiente, i + 1),
                    false
            );
        }
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
