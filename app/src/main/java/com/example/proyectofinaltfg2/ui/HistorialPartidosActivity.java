package com.example.proyectofinaltfg2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.model.Partido;
import com.example.proyectofinaltfg2.repository.PartidoRepository;
import com.example.proyectofinaltfg2.ui.adapters.AdaptadorPartidos;

import java.util.ArrayList;
import java.util.List;

public class HistorialPartidosActivity extends AppCompatActivity {

    private static final String FILTRO_TODOS = "todos";
    private static final String FILTRO_PENDIENTES = "pendientes";
    private static final String FILTRO_CON_RESULTADO = "con_resultado";

    private ImageButton btnVolver;
    private ProgressBar progressHistorial;
    private TextView chipFiltroTodos;
    private TextView chipFiltroPendientes;
    private TextView chipFiltroFinalizados;
    private TextView txtEstadoListaHistorial;
    private LinearLayout containerListaHistorial;

    private PartidoRepository partidoRepository;
    private AdaptadorPartidos adaptadorPartidos;
    private final List<Partido> historialCompleto = new ArrayList<>();
    @NonNull
    private String filtroActual = FILTRO_TODOS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.historial_partidos_layout);

        partidoRepository = new PartidoRepository();
        adaptadorPartidos = new AdaptadorPartidos(this, this::abrirDetallePartido);
        inicializarVistas();
        configurarEventos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarHistorial();
    }

    private void inicializarVistas() {
        btnVolver = findViewById(R.id.btn_volver_historial_partidos);
        progressHistorial = findViewById(R.id.progress_historial_partidos);
        chipFiltroTodos = findViewById(R.id.chip_filtro_todos_historial);
        chipFiltroPendientes = findViewById(R.id.chip_filtro_pendientes_historial);
        chipFiltroFinalizados = findViewById(R.id.chip_filtro_finalizados_historial);
        txtEstadoListaHistorial = findViewById(R.id.txt_estado_lista_historial);
        containerListaHistorial = findViewById(R.id.container_lista_historial);
    }

    private void configurarEventos() {
        btnVolver.setOnClickListener(v -> finish());
        chipFiltroTodos.setOnClickListener(v -> cambiarFiltro(FILTRO_TODOS));
        chipFiltroPendientes.setOnClickListener(v -> cambiarFiltro(FILTRO_PENDIENTES));
        chipFiltroFinalizados.setOnClickListener(v -> cambiarFiltro(FILTRO_CON_RESULTADO));
        actualizarEstiloFiltros();
    }

    private void cambiarFiltro(@NonNull String nuevoFiltro) {
        filtroActual = nuevoFiltro;
        actualizarEstiloFiltros();
        aplicarFiltroHistorial();
    }

    private void actualizarEstiloFiltros() {
        aplicarEstadoChip(chipFiltroTodos, FILTRO_TODOS.equals(filtroActual));
        aplicarEstadoChip(chipFiltroPendientes, FILTRO_PENDIENTES.equals(filtroActual));
        aplicarEstadoChip(chipFiltroFinalizados, FILTRO_CON_RESULTADO.equals(filtroActual));
    }

    private void aplicarEstadoChip(@NonNull TextView chip, boolean seleccionado) {
        chip.setAlpha(seleccionado ? 1f : 0.55f);
    }

    private void cargarHistorial() {
        mostrarCarga(true);
        partidoRepository.obtenerHistorialUsuarioAutenticado(
                this,
                new PartidoRepository.ObtenerPartidosCallback() {
                    @Override
                    public void onSuccess(@NonNull List<Partido> partidos) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        mostrarCarga(false);
                        historialCompleto.clear();
                        historialCompleto.addAll(partidos);
                        aplicarFiltroHistorial();
                    }

                    @Override
                    public void onError(@NonNull String errorMessage) {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        mostrarCarga(false);
                        txtEstadoListaHistorial.setVisibility(View.VISIBLE);
                        txtEstadoListaHistorial.setText(R.string.msg_historial_error_carga);
                        Toast.makeText(HistorialPartidosActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void aplicarFiltroHistorial() {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        List<Partido> partidosFiltrados = new ArrayList<>();
        for (Partido partido : historialCompleto) {
            if (FILTRO_PENDIENTES.equals(filtroActual) && partido.tieneResultadoConfirmado()) {
                continue;
            }
            if (FILTRO_CON_RESULTADO.equals(filtroActual) && !partido.tieneResultadoConfirmado()) {
                continue;
            }
            partidosFiltrados.add(partido);
        }

        adaptadorPartidos.actualizarPartidos(partidosFiltrados);
        adaptadorPartidos.mostrarEnContenedor(containerListaHistorial, true);

        if (partidosFiltrados.isEmpty()) {
            txtEstadoListaHistorial.setVisibility(View.VISIBLE);
            txtEstadoListaHistorial.setText(R.string.msg_historial_sin_resultados);
            return;
        }
        txtEstadoListaHistorial.setVisibility(View.GONE);
    }

    private void mostrarCarga(boolean cargando) {
        progressHistorial.setVisibility(cargando ? View.VISIBLE : View.GONE);
        if (cargando) {
            txtEstadoListaHistorial.setVisibility(View.VISIBLE);
            txtEstadoListaHistorial.setText(R.string.msg_historial_cargando);
            containerListaHistorial.removeAllViews();
        }
    }

    private void abrirDetallePartido(@NonNull Partido partido) {
        if (partido.getIdPartido().isEmpty()) {
            Toast.makeText(this, R.string.msg_partido_id_invalido, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, DetallePartidoActivity.class);
        intent.putExtra(DetallePartidoActivity.EXTRA_PARTIDO_ID, partido.getIdPartido());
        intent.putExtra(DetallePartidoActivity.EXTRA_MODO_HISTORIAL, true);
        startActivity(intent);
    }
}
