package com.example.proyectofinaltfg2.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.logic.PartidoFechaHoraUtils;
import com.example.proyectofinaltfg2.model.Evento;
import com.example.proyectofinaltfg2.repository.EventoRepository;

public class DetalleEventoActivity extends AppCompatActivity {

    public static final String EXTRA_EVENTO_ID = "extra_evento_id";

    private ImageButton btnVolver;
    private TextView txtTitulo;
    private TextView txtDescripcion;
    private TextView txtFecha;
    private TextView txtHora;
    private TextView txtUbicacion;
    private TextView txtClub;
    private TextView txtEstado;

    private EventoRepository eventoRepository;
    @NonNull
    private String eventoId = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalle_evento_layout);

        eventoRepository = new EventoRepository();
        eventoId = extraerEventoId();
        inicializarVistas();
        prepararEventos();
        cargarEvento();
    }

    private void inicializarVistas() {
        btnVolver = findViewById(R.id.btn_volver_detalle_evento);
        txtTitulo = findViewById(R.id.txt_titulo_detalle_evento);
        txtDescripcion = findViewById(R.id.txt_descripcion_detalle_evento);
        txtFecha = findViewById(R.id.txt_fecha_detalle_evento);
        txtHora = findViewById(R.id.txt_hora_detalle_evento);
        txtUbicacion = findViewById(R.id.txt_ubicacion_detalle_evento);
        txtClub = findViewById(R.id.txt_club_detalle_evento);
        txtEstado = findViewById(R.id.txt_estado_detalle_evento);
    }

    private void prepararEventos() {
        btnVolver.setOnClickListener(v -> finish());
    }

    private void cargarEvento() {
        if (TextUtils.isEmpty(eventoId)) {
            Toast.makeText(this, R.string.msg_evento_id_invalido, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        eventoRepository.obtenerEventoPorId(this, eventoId, new EventoRepository.ObtenerEventoCallback() {
            @Override
            public void onSuccess(@NonNull Evento evento) {
                mostrarEvento(evento);
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                Toast.makeText(DetalleEventoActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void mostrarEvento(@NonNull Evento evento) {
        txtTitulo.setText(evento.getTitulo());
        txtDescripcion.setText(evento.getDescripcion());
        txtFecha.setText(PartidoFechaHoraUtils.normalizarFecha(evento.getFecha()));
        txtHora.setText(PartidoFechaHoraUtils.normalizarHora(evento.getHora()));
        txtUbicacion.setText(evento.getUbicacion());
        txtClub.setText(evento.getClubNombre());
        txtEstado.setText(evento.getEstadoPublicacion());
    }

    @NonNull
    private String extraerEventoId() {
        String id = getIntent().getStringExtra(EXTRA_EVENTO_ID);
        if (id == null) {
            return "";
        }
        return id.trim();
    }
}
