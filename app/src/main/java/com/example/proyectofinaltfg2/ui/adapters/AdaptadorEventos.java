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
import com.example.proyectofinaltfg2.model.Evento;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class AdaptadorEventos {

    public interface AlClickEventoListener {
        void onClickEvento(@NonNull Evento evento);
    }

    private final LayoutInflater inflater;
    private final Context context;
    @Nullable
    private final AlClickEventoListener listener;
    private final List<Evento> eventos = new ArrayList<>();

    public AdaptadorEventos(
            @NonNull Context context,
            @Nullable AlClickEventoListener listener
    ) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.listener = listener;
    }

    public void actualizarEventos(@NonNull List<Evento> nuevosEventos) {
        eventos.clear();
        eventos.addAll(nuevosEventos);
    }

    public void mostrarEnContenedor(@NonNull LinearLayout contenedor) {
        contenedor.removeAllViews();
        for (Evento evento : eventos) {
            View vistaItem = inflater.inflate(R.layout.item_evento_layout, contenedor, false);
            enlazarEventoEnVista(vistaItem, evento, listener);
            contenedor.addView(vistaItem);
        }
    }

    private void enlazarEventoEnVista(
            @NonNull View vista,
            @NonNull Evento evento,
            @Nullable AlClickEventoListener listener
    ) {
        MaterialCardView card = vista instanceof MaterialCardView
                ? (MaterialCardView) vista
                : vista.findViewById(R.id.card_item_evento);

        TextView txtTitulo = vista.findViewById(R.id.txt_titulo_item_evento);
        TextView txtUbicacion = vista.findViewById(R.id.txt_ubicacion_item_evento);
        TextView txtFechaHora = vista.findViewById(R.id.txt_fecha_hora_item_evento);
        TextView txtClub = vista.findViewById(R.id.txt_club_item_evento);
        Button btnVerDetalle = vista.findViewById(R.id.btn_ver_detalle_item_evento);

        txtTitulo.setText(evento.getTitulo());
        txtUbicacion.setText(evento.getUbicacion());
        txtFechaHora.setText(
                PartidoFechaHoraUtils.formatearFechaHora(evento.getFecha(), evento.getHora())
        );
        txtClub.setText(context.getString(R.string.evento_club_format, evento.getClubNombre()));

        if (listener == null) {
            if (card != null) {
                card.setOnClickListener(null);
            }
            btnVerDetalle.setOnClickListener(null);
            return;
        }

        View.OnClickListener clickListener = v -> listener.onClickEvento(evento);
        if (card != null) {
            card.setOnClickListener(clickListener);
        }
        btnVerDetalle.setOnClickListener(clickListener);
    }
}
