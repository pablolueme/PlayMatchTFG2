package com.example.proyectofinaltfg2.ui.fragments;

import android.app.DatePickerDialog;
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
import com.example.proyectofinaltfg2.logic.PartidoFechaHoraUtils;
import com.example.proyectofinaltfg2.model.Partido;
import com.example.proyectofinaltfg2.repository.PartidoRepository;
import com.example.proyectofinaltfg2.ui.DetallePartidoActivity;
import com.example.proyectofinaltfg2.ui.adapters.AdaptadorPartidos;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PartidosFragment extends Fragment {

    public static final String MODO_TODOS = "modo_todos";
    public static final String MODO_MIS_PARTIDOS = "modo_mis_partidos";

    private static final String ARG_MODO_LISTADO = "arg_modo_listado";
    private static final String FILTRO_TODOS = "todos";
    private static final String FILTRO_PADEL = Partido.DEPORTE_PADEL;
    private static final String FILTRO_TENIS = Partido.DEPORTE_TENIS;

    private TextView txtTituloPartidos;
    private TextView txtSubtituloPartidos;
    private EditText edtBuscarPartidos;
    private TextView chipFiltroTodos;
    private TextView chipFiltroPadel;
    private TextView chipFiltroTenis;
    private TextView chipFiltroFecha;
    private TextView txtLimpiarFecha;
    private TextView txtEstadoListaPartidos;
    private LinearLayout containerListaPartidos;

    private PartidoRepository partidoRepository;
    private AdaptadorPartidos adaptadorPartidos;
    private final List<Partido> partidosCargados = new ArrayList<>();

    @NonNull
    private String modoListado = MODO_TODOS;
    private String filtroDeporte = FILTRO_TODOS;
    private String fechaFiltroSeleccionada = "";

    private static final DateTimeFormatter FORMATO_FECHA_FILTRO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());

    public PartidosFragment() {
        super(R.layout.partidos_layout);
    }

    @NonNull
    public static PartidosFragment newInstance(@Nullable String modoListado) {
        PartidosFragment fragment = new PartidosFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODO_LISTADO, normalizarModo(modoListado));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            modoListado = normalizarModo(getArguments().getString(ARG_MODO_LISTADO));
        }
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
        actualizarTextosModo();
        actualizarVistaFiltroFecha();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarPartidos();
    }

    public void actualizarModoListado(@Nullable String nuevoModo) {
        String modoNormalizado = normalizarModo(nuevoModo);
        boolean cambioModo = !modoNormalizado.equals(modoListado);
        modoListado = modoNormalizado;
        filtroDeporte = FILTRO_TODOS;
        fechaFiltroSeleccionada = "";
        if (edtBuscarPartidos != null) {
            edtBuscarPartidos.setText("");
        }
        actualizarEstiloFiltros();
        actualizarTextosModo();
        actualizarVistaFiltroFecha();
        if (isAdded()) {
            if (cambioModo || partidosCargados.isEmpty()) {
                cargarPartidos();
            } else {
                aplicarFiltros();
            }
        }
    }

    private void ocultarBottomNavInterna(@NonNull View view) {
        View bottomNav = view.findViewById(R.id.include_bottom_nav_partidos);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }
    }

    private void inicializarVistas(@NonNull View view) {
        txtTituloPartidos = view.findViewById(R.id.txt_titulo_partidos);
        txtSubtituloPartidos = view.findViewById(R.id.txt_subtitulo_partidos);
        edtBuscarPartidos = view.findViewById(R.id.edt_buscar_partidos);
        chipFiltroTodos = view.findViewById(R.id.chip_filtro_todos_partidos);
        chipFiltroPadel = view.findViewById(R.id.chip_filtro_padel_partidos);
        chipFiltroTenis = view.findViewById(R.id.chip_filtro_tenis_partidos);
        chipFiltroFecha = view.findViewById(R.id.chip_filtro_fecha_partidos);
        txtLimpiarFecha = view.findViewById(R.id.txt_limpiar_fecha_partidos);
        txtEstadoListaPartidos = view.findViewById(R.id.txt_estado_lista_partidos);
        containerListaPartidos = view.findViewById(R.id.container_lista_partidos);
    }

    private void actualizarTextosModo() {
        if (!isAdded()) {
            return;
        }
        if (MODO_MIS_PARTIDOS.equals(modoListado)) {
            txtTituloPartidos.setText(R.string.mis_partidos_titulo);
            txtSubtituloPartidos.setText(R.string.mis_partidos_subtitulo);
            edtBuscarPartidos.setHint(R.string.hint_buscar_mis_partidos);
            return;
        }
        txtTituloPartidos.setText(R.string.partidos_titulo);
        txtSubtituloPartidos.setText(R.string.partidos_subtitulo);
        edtBuscarPartidos.setHint(R.string.hint_buscar_partidos);
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

        chipFiltroFecha.setOnClickListener(v -> abrirSelectorFecha());
        txtLimpiarFecha.setOnClickListener(v -> limpiarFiltroFecha());

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

        PartidoRepository.ObtenerPartidosCallback callback = new PartidoRepository.ObtenerPartidosCallback() {
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
        };

        if (MODO_MIS_PARTIDOS.equals(modoListado)) {
            partidoRepository.obtenerMisPartidosActivosYFuturos(requireContext(), callback);
            return;
        }
        partidoRepository.obtenerPartidosActivosYFuturos(requireContext(), callback);
    }

    private void aplicarFiltros() {
        if (!isAdded()) {
            return;
        }

        String textoBusqueda = obtenerTexto(edtBuscarPartidos).toLowerCase(Locale.getDefault());
        List<Partido> partidosFiltrados = new ArrayList<>();
        List<Partido> partidosBase = partidoRepository.filtrarPartidosActivosYFuturosPorFecha(
                partidosCargados,
                fechaFiltroSeleccionada
        );

        for (Partido partido : partidosBase) {
            if (!cumpleFiltroDeporte(partido)) {
                continue;
            }
            if (!cumpleFiltroTexto(partido, textoBusqueda)) {
                continue;
            }
            partidosFiltrados.add(partido);
        }

        adaptadorPartidos.actualizarPartidos(partidosFiltrados);
        adaptadorPartidos.mostrarEnContenedor(containerListaPartidos, true);

        if (partidosFiltrados.isEmpty()) {
            txtEstadoListaPartidos.setVisibility(View.VISIBLE);
            if (MODO_MIS_PARTIDOS.equals(modoListado)) {
                txtEstadoListaPartidos.setText(R.string.msg_mis_partidos_sin_resultados);
            } else {
                txtEstadoListaPartidos.setText(R.string.msg_partidos_sin_resultados);
            }
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

    private void abrirSelectorFecha() {
        if (!isAdded()) {
            return;
        }

        LocalDate fechaInicial = obtenerFechaInicialDatePicker();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    LocalDate fechaSeleccionada = LocalDate.of(year, monthOfYear + 1, dayOfMonth);
                    fechaFiltroSeleccionada = PartidoFechaHoraUtils.normalizarFecha(
                            fechaSeleccionada.format(FORMATO_FECHA_FILTRO)
                    );
                    actualizarVistaFiltroFecha();
                    aplicarFiltros();
                },
                fechaInicial.getYear(),
                fechaInicial.getMonthValue() - 1,
                fechaInicial.getDayOfMonth()
        );
        datePickerDialog.setTitle(R.string.partidos_filtro_fecha_dialogo_titulo);
        datePickerDialog.getDatePicker().setMinDate(obtenerMillisInicioHoy());
        datePickerDialog.show();
    }

    private void limpiarFiltroFecha() {
        if (fechaFiltroSeleccionada.isEmpty()) {
            return;
        }
        fechaFiltroSeleccionada = "";
        actualizarVistaFiltroFecha();
        aplicarFiltros();
    }

    private void actualizarVistaFiltroFecha() {
        if (chipFiltroFecha == null || txtLimpiarFecha == null) {
            return;
        }

        if (fechaFiltroSeleccionada.isEmpty()) {
            chipFiltroFecha.setText(R.string.partidos_filtro_fecha_todas);
            txtLimpiarFecha.setVisibility(View.GONE);
            return;
        }

        chipFiltroFecha.setText(
                getString(R.string.partidos_filtro_fecha_seleccionada, fechaFiltroSeleccionada)
        );
        txtLimpiarFecha.setVisibility(View.VISIBLE);
    }

    @NonNull
    private LocalDate obtenerFechaInicialDatePicker() {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaActual = PartidoFechaHoraUtils.parsearFecha(fechaFiltroSeleccionada);
        if (fechaActual == null || fechaActual.isBefore(hoy)) {
            return hoy;
        }
        return fechaActual;
    }

    private long obtenerMillisInicioHoy() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
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

    @NonNull
    private static String normalizarModo(@Nullable String modo) {
        if (MODO_MIS_PARTIDOS.equals(modo)) {
            return MODO_MIS_PARTIDOS;
        }
        return MODO_TODOS;
    }
}
