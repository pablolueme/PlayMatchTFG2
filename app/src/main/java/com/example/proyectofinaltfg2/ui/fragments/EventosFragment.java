package com.example.proyectofinaltfg2.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.model.Evento;
import com.example.proyectofinaltfg2.model.UserProfile;
import com.example.proyectofinaltfg2.repository.EventoRepository;
import com.example.proyectofinaltfg2.repository.UsuarioRepository;
import com.example.proyectofinaltfg2.ui.CrearEventoActivity;
import com.example.proyectofinaltfg2.ui.DetalleEventoActivity;
import com.example.proyectofinaltfg2.ui.adapters.AdaptadorEventos;
import com.example.proyectofinaltfg2.utils.ValidationUtils;

import java.util.List;

public class EventosFragment extends Fragment {

    private Button btnCrearEvento;
    private ProgressBar progressEventos;
    private TextView txtEstadoListaEventos;
    private LinearLayout containerListaEventos;

    private EventoRepository eventoRepository;
    private UsuarioRepository usuarioRepository;
    private AdaptadorEventos adaptadorEventos;
    private boolean esUsuarioClub = false;

    public EventosFragment() {
        super(R.layout.eventos_layout);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        eventoRepository = new EventoRepository();
        usuarioRepository = new UsuarioRepository();

        View bottomNav = view.findViewById(R.id.include_bottom_nav_eventos);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }

        inicializarVistas(view);
        adaptadorEventos = new AdaptadorEventos(requireContext(), this::abrirDetalleEvento);
        configurarEventos();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarRolUsuarioActual();
        cargarEventosPublicados();
    }

    private void inicializarVistas(@NonNull View view) {
        btnCrearEvento = view.findViewById(R.id.btn_crear_evento_eventos);
        progressEventos = view.findViewById(R.id.progress_eventos);
        txtEstadoListaEventos = view.findViewById(R.id.txt_estado_lista_eventos);
        containerListaEventos = view.findViewById(R.id.container_lista_eventos);
    }

    private void configurarEventos() {
        btnCrearEvento.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CrearEventoActivity.class))
        );
    }

    private void cargarRolUsuarioActual() {
        if (!isAdded()) {
            return;
        }

        usuarioRepository.obtenerUsuarioActual(requireContext(), new UsuarioRepository.UsuarioResultCallback() {
            @Override
            public void onSuccess(@NonNull UserProfile userProfile) {
                esUsuarioClub = ValidationUtils.ROLE_CLUB.equals(userProfile.getRol());
                actualizarBotonCrearEvento();
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                esUsuarioClub = false;
                actualizarBotonCrearEvento();
            }
        });
    }

    private void actualizarBotonCrearEvento() {
        if (!isAdded() || btnCrearEvento == null) {
            return;
        }
        btnCrearEvento.setVisibility(esUsuarioClub ? View.VISIBLE : View.GONE);
    }

    private void cargarEventosPublicados() {
        if (!isAdded()) {
            return;
        }

        mostrarCarga(true);
        eventoRepository.obtenerEventosPublicados(
                requireContext(),
                new EventoRepository.ObtenerEventosCallback() {
                    @Override
                    public void onSuccess(@NonNull List<Evento> eventos) {
                        if (!isAdded()) {
                            return;
                        }
                        mostrarCarga(false);
                        adaptadorEventos.actualizarEventos(eventos);
                        adaptadorEventos.mostrarEnContenedor(containerListaEventos);

                        if (eventos.isEmpty()) {
                            txtEstadoListaEventos.setVisibility(View.VISIBLE);
                            txtEstadoListaEventos.setText(R.string.msg_eventos_sin_resultados);
                            return;
                        }
                        txtEstadoListaEventos.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(@NonNull String errorMessage) {
                        if (!isAdded()) {
                            return;
                        }
                        mostrarCarga(false);
                        txtEstadoListaEventos.setVisibility(View.VISIBLE);
                        txtEstadoListaEventos.setText(R.string.msg_eventos_error_carga);
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void mostrarCarga(boolean cargando) {
        progressEventos.setVisibility(cargando ? View.VISIBLE : View.GONE);
        if (cargando) {
            txtEstadoListaEventos.setVisibility(View.VISIBLE);
            txtEstadoListaEventos.setText(R.string.msg_eventos_cargando);
            containerListaEventos.removeAllViews();
        }
    }

    private void abrirDetalleEvento(@NonNull Evento evento) {
        if (!isAdded()) {
            return;
        }
        if (evento.getIdEvento().isEmpty()) {
            Toast.makeText(requireContext(), R.string.msg_evento_id_invalido, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(requireContext(), DetalleEventoActivity.class);
        intent.putExtra(DetalleEventoActivity.EXTRA_EVENTO_ID, evento.getIdEvento());
        startActivity(intent);
    }
}
