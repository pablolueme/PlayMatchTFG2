package com.example.proyectofinaltfg2.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.logic.PartidoFechaHoraUtils;
import com.example.proyectofinaltfg2.model.Evento;
import com.example.proyectofinaltfg2.model.UserProfile;
import com.example.proyectofinaltfg2.service.AuthService;
import com.example.proyectofinaltfg2.utils.FirebaseAuthUtil;
import com.example.proyectofinaltfg2.utils.ValidationUtils;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class EventoRepository {

    private static final String EVENTOS_COLLECTION = "eventos";

    private final FirebaseFirestore firestore;
    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;

    public interface CrearEventoCallback {
        void onSuccess(@NonNull Evento evento);

        void onError(@NonNull String errorMessage);
    }

    public interface ObtenerEventosCallback {
        void onSuccess(@NonNull List<Evento> eventos);

        void onError(@NonNull String errorMessage);
    }

    public interface ObtenerEventoCallback {
        void onSuccess(@NonNull Evento evento);

        void onError(@NonNull String errorMessage);
    }

    public interface ObtenerProximoEventoCallback {
        void onSuccess(@Nullable Evento evento);

        void onError(@NonNull String errorMessage);
    }

    public EventoRepository() {
        this(FirebaseFirestore.getInstance(), new UsuarioRepository(), new AuthService());
    }

    public EventoRepository(
            @NonNull FirebaseFirestore firestore,
            @NonNull UsuarioRepository usuarioRepository,
            @NonNull AuthService authService
    ) {
        this.firestore = firestore;
        this.usuarioRepository = usuarioRepository;
        this.authService = authService;
    }

    public void crearEvento(
            @NonNull Context context,
            @NonNull String titulo,
            @NonNull String descripcion,
            @NonNull String fecha,
            @NonNull String hora,
            @NonNull String ubicacion,
            @NonNull CrearEventoCallback callback
    ) {
        String tituloSeguro = limpiarTexto(titulo);
        String descripcionSegura = limpiarTexto(descripcion);
        String fechaSegura = limpiarTexto(fecha);
        String horaSegura = limpiarTexto(hora);
        String ubicacionSegura = limpiarTexto(ubicacion);

        if (tituloSeguro.isEmpty()
                || descripcionSegura.isEmpty()
                || fechaSegura.isEmpty()
                || horaSegura.isEmpty()
                || ubicacionSegura.isEmpty()) {
            callback.onError(context.getString(R.string.msg_evento_campos_obligatorios));
            return;
        }

        if (PartidoFechaHoraUtils.parsearFecha(fechaSegura) == null) {
            callback.onError(context.getString(R.string.msg_evento_fecha_invalida));
            return;
        }
        if (PartidoFechaHoraUtils.parsearHora(horaSegura) == null) {
            callback.onError(context.getString(R.string.msg_evento_hora_invalida));
            return;
        }

        if (!authService.isUsuarioAutenticado()) {
            callback.onError(context.getString(R.string.msg_auth_invalid_credentials));
            return;
        }

        usuarioRepository.obtenerUsuarioActual(context, new UsuarioRepository.UsuarioResultCallback() {
            @Override
            public void onSuccess(@NonNull UserProfile userProfile) {
                if (!ValidationUtils.ROLE_CLUB.equals(userProfile.getRol())) {
                    callback.onError(context.getString(R.string.msg_eventos_solo_club_crear));
                    return;
                }

                String clubId = limpiarTexto(userProfile.getUid());
                if (clubId.isEmpty()) {
                    clubId = authService.getUsuarioIdActual();
                }
                if (clubId.isEmpty()) {
                    callback.onError(context.getString(R.string.msg_auth_invalid_credentials));
                    return;
                }

                Evento evento = construirEvento(
                        tituloSeguro,
                        descripcionSegura,
                        fechaSegura,
                        horaSegura,
                        ubicacionSegura,
                        clubId,
                        obtenerNombreClub(context, userProfile)
                );

                DocumentReference documentReference =
                        firestore.collection(EVENTOS_COLLECTION).document();
                evento.setIdEvento(documentReference.getId());

                Date now = new Date();
                evento.setFechaCreacion(now);
                evento.setUltimaActualizacion(now);

                documentReference
                        .set(evento.toMap())
                        .addOnSuccessListener(unused -> callback.onSuccess(evento))
                        .addOnFailureListener(exception ->
                                callback.onError(FirebaseAuthUtil.getAuthErrorMessage(context, exception))
                        );
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public void obtenerEventosPublicados(
            @NonNull Context context,
            @NonNull ObtenerEventosCallback callback
    ) {
        firestore
                .collection(EVENTOS_COLLECTION)
                .whereEqualTo("estadoPublicacion", Evento.ESTADO_PUBLICADO)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Evento> eventos = new ArrayList<>();
                    queryDocumentSnapshots.getDocuments().forEach(documentSnapshot -> {
                        Evento evento = Evento.fromDocument(documentSnapshot);
                        if (evento == null) {
                            return;
                        }
                        if (!evento.estaPublicado()) {
                            return;
                        }
                        eventos.add(evento);
                    });

                    eventos.sort(
                            Comparator.comparing(
                                    (Evento evento) -> PartidoFechaHoraUtils.parsearFechaHora(
                                            evento.getFecha(),
                                            evento.getHora()
                                    ),
                                    Comparator.nullsLast(Comparator.reverseOrder())
                            ).thenComparing(
                                    Evento::getFechaCreacion,
                                    Comparator.nullsLast(Comparator.reverseOrder())
                            )
                    );
                    callback.onSuccess(eventos);
                })
                .addOnFailureListener(exception ->
                        callback.onError(FirebaseAuthUtil.getAuthErrorMessage(context, exception))
                );
    }

    public void obtenerEventoPorId(
            @NonNull Context context,
            @NonNull String eventoId,
            @NonNull ObtenerEventoCallback callback
    ) {
        String idSeguro = limpiarTexto(eventoId);
        if (idSeguro.isEmpty()) {
            callback.onError(context.getString(R.string.msg_evento_id_invalido));
            return;
        }

        firestore
                .collection(EVENTOS_COLLECTION)
                .document(idSeguro)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Evento evento = Evento.fromDocument(documentSnapshot);
                    if (evento == null) {
                        callback.onError(context.getString(R.string.msg_evento_no_encontrado));
                        return;
                    }
                    callback.onSuccess(evento);
                })
                .addOnFailureListener(exception ->
                        callback.onError(FirebaseAuthUtil.getAuthErrorMessage(context, exception))
                );
    }

    public void obtenerProximoEventoPublicado(
            @NonNull Context context,
            @NonNull ObtenerProximoEventoCallback callback
    ) {
        firestore
                .collection(EVENTOS_COLLECTION)
                .whereEqualTo("estadoPublicacion", Evento.ESTADO_PUBLICADO)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Evento proximoEvento = null;
                    LocalDateTime fechaHoraProxima = null;
                    LocalDateTime ahora = LocalDateTime.now();

                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        Evento evento = Evento.fromDocument(queryDocumentSnapshots.getDocuments().get(i));
                        if (evento == null || !evento.estaPublicado()) {
                            continue;
                        }

                        LocalDateTime fechaHoraEvento = PartidoFechaHoraUtils.parsearFechaHora(
                                evento.getFecha(),
                                evento.getHora()
                        );
                        if (fechaHoraEvento == null || fechaHoraEvento.isBefore(ahora)) {
                            continue;
                        }

                        if (fechaHoraProxima == null || fechaHoraEvento.isBefore(fechaHoraProxima)) {
                            fechaHoraProxima = fechaHoraEvento;
                            proximoEvento = evento;
                        }
                    }

                    callback.onSuccess(proximoEvento);
                })
                .addOnFailureListener(exception ->
                        callback.onError(FirebaseAuthUtil.getAuthErrorMessage(context, exception))
                );
    }

    @NonNull
    private Evento construirEvento(
            @NonNull String titulo,
            @NonNull String descripcion,
            @NonNull String fecha,
            @NonNull String hora,
            @NonNull String ubicacion,
            @NonNull String clubId,
            @NonNull String clubNombre
    ) {
        Evento evento = new Evento();
        evento.setTitulo(titulo);
        evento.setDescripcion(descripcion);
        evento.setFecha(PartidoFechaHoraUtils.normalizarFecha(fecha));
        evento.setHora(PartidoFechaHoraUtils.normalizarHora(hora));
        evento.setUbicacion(ubicacion);
        evento.setClubId(clubId);
        evento.setClubNombre(clubNombre);
        evento.setEstadoPublicacion(Evento.ESTADO_PUBLICADO);
        return evento;
    }

    @NonNull
    private String obtenerNombreClub(@NonNull Context context, @NonNull UserProfile userProfile) {
        String nombreClub = limpiarTexto(userProfile.getNombreClub());
        if (!nombreClub.isEmpty()) {
            return nombreClub;
        }

        String alias = limpiarTexto(userProfile.getAlias());
        if (!alias.isEmpty()) {
            return alias;
        }

        String nombre = limpiarTexto(userProfile.getNombre());
        if (!nombre.isEmpty()) {
            return nombre;
        }

        return context.getString(R.string.home_nombre_no_disponible);
    }

    @NonNull
    private String limpiarTexto(@Nullable String value) {
        return value == null ? "" : value.trim();
    }
}
