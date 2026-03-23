package com.example.proyectofinaltfg2.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.logic.PartidoFechaHoraUtils;
import com.example.proyectofinaltfg2.model.Partido;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdaptadorPartidos {

    public interface AlClickPartidoListener {
        void onClickPartido(@NonNull Partido partido);
    }

    private final LayoutInflater inflater;
    @Nullable
    private final AlClickPartidoListener listener;
    private final List<Partido> partidos = new ArrayList<>();

    public AdaptadorPartidos(
            @NonNull Context context,
            @Nullable AlClickPartidoListener listener
    ) {
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    public void actualizarPartidos(@NonNull List<Partido> nuevosPartidos) {
        partidos.clear();
        partidos.addAll(nuevosPartidos);
    }

    public void mostrarEnContenedor(@NonNull LinearLayout contenedor) {
        mostrarEnContenedor(contenedor, false);
    }

    public void mostrarEnContenedor(@NonNull LinearLayout contenedor, boolean agruparPorFecha) {
        contenedor.removeAllViews();
        String fechaActual = "";
        for (Partido partido : partidos) {
            String fechaPartido = PartidoFechaHoraUtils.normalizarFecha(partido.getFecha());
            if (agruparPorFecha && !fechaPartido.equals(fechaActual)) {
                agregarCabeceraFecha(contenedor, fechaPartido);
                fechaActual = fechaPartido;
            }
            View vistaItem = inflater.inflate(R.layout.item_partido_layout, contenedor, false);
            enlazarPartidoEnVista(vistaItem, partido, false, listener);
            contenedor.addView(vistaItem);
        }
    }

    private void agregarCabeceraFecha(@NonNull LinearLayout contenedor, @NonNull String fechaPartido) {
        View cabecera = inflater.inflate(R.layout.item_fecha_partido_header, contenedor, false);
        TextView txtFecha = cabecera.findViewById(R.id.txt_fecha_header_partidos);
        txtFecha.setText(cabecera.getContext().getString(R.string.partidos_header_fecha, fechaPartido));
        contenedor.addView(cabecera);
    }

    public static void enlazarPartidoEnVista(
            @NonNull View vista,
            @NonNull Partido partido,
            boolean mostrarBotonApuntarse,
            @Nullable AlClickPartidoListener listener
    ) {
        MaterialCardView card = vista instanceof MaterialCardView
                ? (MaterialCardView) vista
                : vista.findViewById(R.id.card_item_partido);

        TextView txtChipPlazas = vista.findViewById(R.id.txt_chip_plazas_item);
        TextView txtTitulo = vista.findViewById(R.id.txt_titulo_item_partido);
        TextView txtUbicacion = vista.findViewById(R.id.txt_ubicacion_item_partido);
        TextView txtFechaHora = vista.findViewById(R.id.txt_fecha_hora_item_partido);
        TextView txtNivel = vista.findViewById(R.id.txt_nivel_item_partido);
        Button btnApuntarse = vista.findViewById(R.id.btn_apuntarse_item_partido);

        txtTitulo.setText(vista.getContext().getString(
                R.string.partido_titulo_item_format,
                capitalizar(partido.getDeporte())
        ));
        txtUbicacion.setText(partido.getDireccion());
        txtFechaHora.setText(
                PartidoFechaHoraUtils.formatearFechaHora(partido.getFecha(), partido.getHora())
        );
        txtNivel.setText(vista.getContext().getString(R.string.partido_nivel_format, partido.getNivel()));
        txtChipPlazas.setText(vista.getContext().getString(
                R.string.partido_plazas_ocupadas_format,
                partido.getPlazasOcupadas(),
                partido.getMaxJugadores()
        ));

        int fondoNivel = Partido.DEPORTE_TENIS.equals(partido.getDeporte())
                ? R.drawable.bg_chip_tenis
                : R.drawable.bg_chip_padel;
        txtNivel.setBackgroundResource(fondoNivel);

        btnApuntarse.setEnabled(false);
        btnApuntarse.setVisibility(mostrarBotonApuntarse ? View.VISIBLE : View.GONE);

        if (card == null) {
            return;
        }
        if (listener == null) {
            card.setOnClickListener(null);
            return;
        }
        card.setOnClickListener(v -> listener.onClickPartido(partido));
    }

    @NonNull
    private static String capitalizar(@Nullable String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return "";
        }
        String valor = texto.trim();
        return valor.substring(0, 1).toUpperCase(Locale.getDefault())
                + valor.substring(1).toLowerCase(Locale.getDefault());
    }
}
