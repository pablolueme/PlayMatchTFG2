package com.example.proyectofinaltfg2.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.logic.PartidoFechaHoraUtils;
import com.example.proyectofinaltfg2.model.Partido;
import com.example.proyectofinaltfg2.model.UserProfile;
import com.example.proyectofinaltfg2.repository.PartidoRepository;
import com.example.proyectofinaltfg2.repository.UsuarioRepository;
import com.example.proyectofinaltfg2.ui.CrearPartidoActivity;
import com.example.proyectofinaltfg2.ui.DetallePartidoActivity;
import com.example.proyectofinaltfg2.ui.HomeActivity;
import com.example.proyectofinaltfg2.ui.adapters.AdaptadorPartidos;
import com.example.proyectofinaltfg2.utils.ValidationUtils;

import java.util.List;
import java.util.Locale;

public class InicioFragment extends Fragment {

    private TextView txtAliasHome;
    private TextView txtEstadoHome;
    private ImageView imgBusquedaHome;

    private LinearLayout cardCrearPartidoHome;
    private LinearLayout cardBuscarPartidosHome;
    private LinearLayout cardMisPartidosHome;
    private LinearLayout cardProximoPartidoHome;
    private TextView txtFechaProximoPartidoHome;
    private TextView txtLugarProximoPartidoHome;
    private TextView txtDeporteProximoPartidoHome;
    private TextView txtPlazasProximoPartidoHome;
    private Button btnVerPartidoHome;
    private View itemPartidoPreviewHome;

    private UsuarioRepository usuarioRepository;
    private PartidoRepository partidoRepository;
    @Nullable
    private Partido partidoDestacado;

    public InicioFragment() {
        super(R.layout.home_layout);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        usuarioRepository = crearUsuarioRepository();
        partidoRepository = crearPartidoRepository();

        ocultarBottomNavInterna(view);
        inicializarVistas(view);
        configurarEventos();
        mostrarHomeSinPartidos();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarCabeceraPerfil();
        cargarPartidosHome();
    }

    private void ocultarBottomNavInterna(@NonNull View view) {
        View bottomNav = view.findViewById(R.id.include_bottom_nav_home);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }
    }

    private void inicializarVistas(@NonNull View view) {
        txtAliasHome = view.findViewById(R.id.txt_alias_home);
        txtEstadoHome = view.findViewById(R.id.txt_estado_home);
        imgBusquedaHome = view.findViewById(R.id.img_busqueda_home);
        cardCrearPartidoHome = view.findViewById(R.id.card_crear_partido_home);
        cardBuscarPartidosHome = view.findViewById(R.id.card_buscar_partidos_home);
        cardMisPartidosHome = view.findViewById(R.id.card_mis_partidos_home);
        cardProximoPartidoHome = view.findViewById(R.id.card_proximo_partido_home);
        txtFechaProximoPartidoHome = view.findViewById(R.id.txt_fecha_proximo_partido_home);
        txtLugarProximoPartidoHome = view.findViewById(R.id.txt_lugar_proximo_partido_home);
        txtDeporteProximoPartidoHome = view.findViewById(R.id.txt_deporte_proximo_partido_home);
        txtPlazasProximoPartidoHome = view.findViewById(R.id.txt_plazas_proximo_partido_home);
        btnVerPartidoHome = view.findViewById(R.id.btn_ver_partido_home);
        itemPartidoPreviewHome = view.findViewById(R.id.item_partido_preview_home);
    }

    private void configurarEventos() {
        cardCrearPartidoHome.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), CrearPartidoActivity.class))
        );
        cardBuscarPartidosHome.setOnClickListener(v -> abrirPartidosDesdeHome(false));
        cardMisPartidosHome.setOnClickListener(v -> abrirPartidosDesdeHome(true));
        imgBusquedaHome.setOnClickListener(v -> abrirPartidosDesdeHome(false));

        cardProximoPartidoHome.setOnClickListener(v -> abrirDetallePartido(partidoDestacado));
        btnVerPartidoHome.setOnClickListener(v -> abrirDetallePartido(partidoDestacado));
    }

    private void abrirPartidosDesdeHome(boolean soloMisPartidos) {
        if (!isAdded()) {
            return;
        }
        if (requireActivity() instanceof HomeActivity) {
            ((HomeActivity) requireActivity()).abrirPartidos(soloMisPartidos);
        }
    }

    private void cargarCabeceraPerfil() {
        if (!isAdded()) {
            return;
        }

        usuarioRepository.obtenerUsuarioActual(requireContext(), new UsuarioRepository.UsuarioResultCallback() {
            @Override
            public void onSuccess(@NonNull UserProfile userProfile) {
                mostrarCabeceraPerfil(
                        userProfile.getAlias(),
                        userProfile.getNombre(),
                        userProfile.getRol()
                );
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                txtAliasHome.setText(R.string.home_bienvenida_default);
                txtEstadoHome.setText(errorMessage);
            }
        });
    }

    private void mostrarCabeceraPerfil(
            @Nullable String alias,
            @Nullable String nombreCompleto,
            @Nullable String rol
    ) {
        if (TextUtils.isEmpty(alias)) {
            txtAliasHome.setText(R.string.home_bienvenida_default);
        } else {
            txtAliasHome.setText(alias);
        }

        String nombreSeguro = TextUtils.isEmpty(nombreCompleto)
                ? getString(R.string.home_nombre_no_disponible)
                : nombreCompleto;
        String rolSeguro = TextUtils.isEmpty(rol) ? ValidationUtils.ROLE_USER : rol;
        txtEstadoHome.setText(getString(R.string.home_profile_detail, nombreSeguro, rolSeguro));
    }

    private void cargarPartidosHome() {
        if (!isAdded()) {
            return;
        }

        partidoRepository.obtenerProximosPartidosActivos(
                requireContext(),
                3,
                new PartidoRepository.ObtenerPartidosCallback() {
                    @Override
                    public void onSuccess(@NonNull List<Partido> partidos) {
                        if (!isAdded()) {
                            return;
                        }
                        mostrarPartidosHome(partidos);
                    }

                    @Override
                    public void onError(@NonNull String errorMessage) {
                        if (!isAdded()) {
                            return;
                        }
                        mostrarHomeSinPartidos();
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void mostrarPartidosHome(@NonNull List<Partido> partidos) {
        if (partidos.isEmpty()) {
            mostrarHomeSinPartidos();
            return;
        }

        partidoDestacado = partidos.get(0);
        txtFechaProximoPartidoHome.setText(
                PartidoFechaHoraUtils.formatearFechaHora(
                        partidoDestacado.getFecha(),
                        partidoDestacado.getHora()
                )
        );
        txtLugarProximoPartidoHome.setText(partidoDestacado.getDireccion());
        txtDeporteProximoPartidoHome.setText(capitalizar(partidoDestacado.getDeporte()));
        txtPlazasProximoPartidoHome.setText(getString(
                R.string.partido_plazas_libres_format,
                partidoDestacado.getPlazasLibres()
        ));
        btnVerPartidoHome.setEnabled(true);

        if (partidos.size() > 1) {
            itemPartidoPreviewHome.setVisibility(View.VISIBLE);
            AdaptadorPartidos.enlazarPartidoEnVista(
                    itemPartidoPreviewHome,
                    partidos.get(1),
                    false,
                    this::abrirDetallePartido
            );
            return;
        }

        itemPartidoPreviewHome.setVisibility(View.GONE);
    }

    private void mostrarHomeSinPartidos() {
        partidoDestacado = null;
        txtFechaProximoPartidoHome.setText(R.string.home_sin_partidos_fecha);
        txtLugarProximoPartidoHome.setText(R.string.home_sin_partidos_lugar);
        txtDeporteProximoPartidoHome.setText(R.string.home_sin_partidos_deporte);
        txtPlazasProximoPartidoHome.setText(R.string.home_sin_partidos_plazas);
        btnVerPartidoHome.setEnabled(false);
        itemPartidoPreviewHome.setVisibility(View.GONE);
    }

    @VisibleForTesting
    @NonNull
    protected UsuarioRepository crearUsuarioRepository() {
        return new UsuarioRepository();
    }

    @VisibleForTesting
    @NonNull
    protected PartidoRepository crearPartidoRepository() {
        return new PartidoRepository();
    }

    private void abrirDetallePartido(@Nullable Partido partido) {
        if (!isAdded() || partido == null) {
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
    private String capitalizar(@Nullable String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return "";
        }
        String valor = texto.trim();
        return valor.substring(0, 1).toUpperCase(Locale.getDefault())
                + valor.substring(1).toLowerCase(Locale.getDefault());
    }
}
