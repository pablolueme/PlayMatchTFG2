package com.example.proyectofinaltfg2.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.model.Partido;
import com.example.proyectofinaltfg2.repository.PartidoRepository;
import com.example.proyectofinaltfg2.ui.DetallePartidoActivity;
import com.example.proyectofinaltfg2.ui.adapters.AdaptadorPartidos;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PartidosFragment extends Fragment {

    private static final String FILTRO_TODOS = "todos";
    private static final String FILTRO_PADEL = Partido.DEPORTE_PADEL;
    private static final String FILTRO_TENIS = Partido.DEPORTE_TENIS;

    private EditText edtBuscarPartidos;
    private TextView chipFiltroTodos;
    private TextView chipFiltroPadel;
    private TextView chipFiltroTenis;
    private TextView txtEstadoListaPartidos;
    private LinearLayout containerListaPartidos;

    private PartidoRepository partidoRepository;
    private AdaptadorPartidos adaptadorPartidos;
    private final List<Partido> partidosCargados = new ArrayList<>();

    private String filtroDeporte = FILTRO_TODOS;

    public PartidosFragment() {
        super(R.layout.partidos_layout);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        partidoRepository = new PartidoRepository();
        ocultarBottomNavInterna(view);
        inicializarVistas(view);
        adaptadorPartidos = new AdaptadorPartidos(requireContext(), this::abrirDetallePartido);
        configurarEventos();
        actualizarEstiloFiltros();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarPartidos();
    }

    private void ocultarBottomNavInterna(@NonNull View view) {
        View bottomNav = view.findViewById(R.id.include_bottom_nav_partidos);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }
    }

    private void inicializarVistas(@NonNull View view) {
        edtBuscarPartidos = view.findViewById(R.id.edt_buscar_partidos);
        chipFiltroTodos = view.findViewById(R.id.chip_filtro_todos_partidos);
        chipFiltroPadel = view.findViewById(R.id.chip_filtro_padel_partidos);
        chipFiltroTenis = view.findViewById(R.id.chip_filtro_tenis_partidos);
        txtEstadoListaPartidos = view.findViewById(R.id.txt_estado_lista_partidos);
        containerListaPartidos = view.findViewById(R.id.container_lista_partidos);
    }

    private void configurarEventos() {
        chipFiltroTodos.setOnClickListener(v -> {
            filtroDeporte = FILTRO_TODOS;
            actualizarEstiloFiltros();
            aplicarFiltros();
        });

        chipFiltroPadel.setOnClickListener(v -> {
            filtroDeporte = FILTRO_PADEL;
            actualizarEstiloFiltros();
            aplicarFiltros();
        });

        chipFiltroTenis.setOnClickListener(v -> {
            filtroDeporte = FILTRO_TENIS;
            actualizarEstiloFiltros();
            aplicarFiltros();
        });

        edtBuscarPartidos.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No-op.
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No-op.
            }

            @Override
            public void afterTextChanged(Editable s) {
                aplicarFiltros();
            }
        });
    }

    private void cargarPartidos() {
        if (!isAdded()) {
            return;
        }

        txtEstadoListaPartidos.setVisibility(View.VISIBLE);
        txtEstadoListaPartidos.setText(R.string.msg_partidos_cargando);

        partidoRepository.obtenerPartidosActivosYFuturos(
                requireContext(),
                new PartidoRepository.ObtenerPartidosCallback() {
                    @Override
                    public void onSuccess(@NonNull List<Partido> partidos) {
                        if (!isAdded()) {
                            return;
                        }
                        partidosCargados.clear();
                        partidosCargados.addAll(partidos);
                        aplicarFiltros();
                    }

                    @Override
                    public void onError(@NonNull String errorMessage) {
                        if (!isAdded()) {
                            return;
                        }
                        txtEstadoListaPartidos.setVisibility(View.VISIBLE);
                        txtEstadoListaPartidos.setText(R.string.msg_partidos_error_carga);
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void aplicarFiltros() {
        if (!isAdded()) {
            return;
        }

        String textoBusqueda = obtenerTexto(edtBuscarPartidos).toLowerCase(Locale.getDefault());
        List<Partido> partidosFiltrados = new ArrayList<>();

        for (Partido partido : partidosCargados) {
            if (!cumpleFiltroDeporte(partido)) {
                continue;
            }
            if (!cumpleFiltroTexto(partido, textoBusqueda)) {
                continue;
            }
            partidosFiltrados.add(partido);
        }

        adaptadorPartidos.actualizarPartidos(partidosFiltrados);
        adaptadorPartidos.mostrarEnContenedor(containerListaPartidos);

        if (partidosFiltrados.isEmpty()) {
            txtEstadoListaPartidos.setVisibility(View.VISIBLE);
            txtEstadoListaPartidos.setText(R.string.msg_partidos_sin_resultados);
        } else {
            txtEstadoListaPartidos.setVisibility(View.GONE);
        }
    }

    private boolean cumpleFiltroDeporte(@NonNull Partido partido) {
        if (FILTRO_TODOS.equals(filtroDeporte)) {
            return true;
        }
        return filtroDeporte.equalsIgnoreCase(partido.getDeporte());
    }

    private boolean cumpleFiltroTexto(@NonNull Partido partido, @NonNull String textoBusqueda) {
        if (textoBusqueda.isEmpty()) {
            return true;
        }

        String textoPartido = (
                partido.getDeporte() + " " +
                        partido.getDireccion() + " " +
                        partido.getNivel() + " " +
                        partido.getFecha() + " " +
                        partido.getHora()
        ).toLowerCase(Locale.getDefault());

        return textoPartido.contains(textoBusqueda);
    }

    private void actualizarEstiloFiltros() {
        aplicarEstadoFiltro(chipFiltroTodos, FILTRO_TODOS.equals(filtroDeporte));
        aplicarEstadoFiltro(chipFiltroPadel, FILTRO_PADEL.equals(filtroDeporte));
        aplicarEstadoFiltro(chipFiltroTenis, FILTRO_TENIS.equals(filtroDeporte));
    }

    private void aplicarEstadoFiltro(@NonNull TextView chip, boolean seleccionado) {
        chip.setAlpha(seleccionado ? 1f : 0.55f);
    }

    private void abrirDetallePartido(@NonNull Partido partido) {
        if (!isAdded()) {
            return;
        }
        if (partido.getIdPartido().isEmpty()) {
            Toast.makeText(requireContext(), R.string.msg_partido_id_invalido, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireContext(), DetallePartidoActivity.class);
        intent.putExtra(DetallePartidoActivity.EXTRA_PARTIDO_ID, partido.getIdPartido());
        startActivity(intent);
    }

    @NonNull
    private String obtenerTexto(@NonNull EditText editText) {
        if (editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}
