package com.example.proyectofinaltfg2.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.logic.PartidoFechaHoraUtils;
import com.example.proyectofinaltfg2.logic.ValidadorPartido;
import com.example.proyectofinaltfg2.model.Partido;
import com.example.proyectofinaltfg2.model.Resultado;
import com.example.proyectofinaltfg2.model.UserProfile;
import com.example.proyectofinaltfg2.model.Usuario;
import com.example.proyectofinaltfg2.service.AuthService;
import com.example.proyectofinaltfg2.utils.FirebaseAuthUtil;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartidoRepository {

    private static final String PARTIDOS_COLLECTION = "partidos";
    private static final int DIAS_LIMPIEZA_INACTIVOS = 2;
    private static final List<String> SETS_VALIDOS = Arrays.asList(
            "6-0", "6-1", "6-2", "6-3", "6-4", "7-5", "7-6",
            "0-6", "1-6", "2-6", "3-6", "4-6", "5-7", "6-7"
    );

    private final FirebaseFirestore firestore;
    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;

    public interface CrearPartidoCallback {
        void onSuccess(@NonNull Partido partido);

        void onError(@NonNull String errorMessage);
    }

    public interface ObtenerPartidoCallback {
        void onSuccess(@NonNull Partido partido);

        void onError(@NonNull String errorMessage);
    }

    public interface ObtenerPartidosCallback {
        void onSuccess(@NonNull List<Partido> partidos);

        void onError(@NonNull String errorMessage);
    }

    public interface GestionInscripcionCallback {
        void onSuccess(@NonNull Partido partidoActualizado, @NonNull String mensajeExito);

        void onError(@NonNull String errorMessage);
    }

    public interface ConfirmarResultadoCallback {
        void onSuccess(@NonNull Partido partidoActualizado, @NonNull String mensajeExito);

        void onError(@NonNull String errorMessage);
    }

    public PartidoRepository() {
        this(FirebaseFirestore.getInstance(), new UsuarioRepository(), new AuthService());
    }

    public PartidoRepository(
            @NonNull FirebaseFirestore firestore,
            @NonNull UsuarioRepository usuarioRepository,
            @NonNull AuthService authService
    ) {
        this.firestore = firestore;
        this.usuarioRepository = usuarioRepository;
        this.authService = authService;
    }

    public void crearPartido(
            @NonNull Context context,
            @NonNull String deporte,
            @NonNull String fecha,
            @NonNull String hora,
            @NonNull String direccion,
            @NonNull String nivel,
            int personasIniciales,
            @NonNull CrearPartidoCallback callback
    ) {
        ValidadorPartido.ResultadoValidacion resultadoValidacion =
                ValidadorPartido.validarFormularioCrear(
                        deporte,
                        fecha,
                        hora,
                        direccion,
                        nivel,
                        personasIniciales
                );
        if (!resultadoValidacion.esValido()) {
            callback.onError(context.getString(resultadoValidacion.getMensajeResId()));
            return;
        }

        if (!authService.isUsuarioAutenticado()) {
            callback.onError(context.getString(R.string.msg_auth_invalid_credentials));
            return;
        }

        usuarioRepository.obtenerUsuarioActual(context, new UsuarioRepository.UsuarioResultCallback() {
            @Override
            public void onSuccess(@NonNull UserProfile userProfile) {
                Usuario usuario = Usuario.fromUserProfile(userProfile);
                String creadorId = usuario.getId();
                if (creadorId.isEmpty()) {
                    creadorId = authService.getUsuarioIdActual();
                }
                if (creadorId.isEmpty()) {
                    callback.onError(context.getString(R.string.msg_auth_invalid_credentials));
                    return;
                }

                Partido partido = construirPartido(
                        deporte,
                        fecha,
                        hora,
                        direccion,
                        nivel,
                        personasIniciales,
                        creadorId,
                        obtenerNombreCreador(context, usuario)
                );
                if (partido.getPlazasOcupadas() >= partido.getMaxJugadores()) {
                    callback.onError(context.getString(R.string.error_partido_plazas_iniciales_invalid));
                    return;
                }

                DocumentReference documentReference =
                        firestore.collection(PARTIDOS_COLLECTION).document();
                partido.setIdPartido(documentReference.getId());

                Date now = new Date();
                partido.setFechaCreacion(now);
                partido.setUltimaActualizacion(now);

                documentReference
                        .set(partido.toMap())
                        .addOnSuccessListener(unused -> callback.onSuccess(partido))
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

    public void obtenerPartidosActivosYFuturos(
            @NonNull Context context,
            @NonNull ObtenerPartidosCallback callback
    ) {
        limpiarPartidosInactivosAntiguos();
        String usuarioIdActual = authService.getUsuarioIdActual();

        firestore
                .collection(PARTIDOS_COLLECTION)
                .whereIn("estado", Arrays.asList(Partido.ESTADO_ACTIVO, Partido.ESTADO_COMPLETO))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Partido> partidosFuturos = new ArrayList<>();
                    List<String> partidosPasados = new ArrayList<>();

                    queryDocumentSnapshots.getDocuments().forEach(documentSnapshot -> {
                        Partido partido = Partido.fromDocument(documentSnapshot);
                        if (partido == null) {
                            return;
                        }

                        if (PartidoFechaHoraUtils.esFuturoOPresente(partido.getFecha(), partido.getHora())) {
                            if (!debeMostrarseEnListado(partido, usuarioIdActual)) {
                                return;
                            }
                            partidosFuturos.add(partido);
                            return;
                        }

                        if (!partido.getIdPartido().isEmpty()) {
                            partidosPasados.add(partido.getIdPartido());
                        }
                    });

                    partidosFuturos.sort(
                            Comparator.comparing(
                                    partido -> PartidoFechaHoraUtils.parsearFechaHora(
                                            partido.getFecha(),
                                            partido.getHora()
                                    ),
                                    Comparator.nullsLast(Comparator.naturalOrder())
                            )
                    );

                    callback.onSuccess(partidosFuturos);
                    marcarPartidosPasadosComoFinalizados(partidosPasados);
                })
                .addOnFailureListener(exception ->
                        callback.onError(FirebaseAuthUtil.getAuthErrorMessage(context, exception))
                );
    }

    public void obtenerMisPartidosActivosYFuturos(
            @NonNull Context context,
            @NonNull ObtenerPartidosCallback callback
    ) {
        limpiarPartidosInactivosAntiguos();
        String usuarioIdActual = authService.getUsuarioIdActual();
        if (isBlank(usuarioIdActual)) {
            callback.onError(context.getString(R.string.msg_auth_invalid_credentials));
            return;
        }

        firestore
                .collection(PARTIDOS_COLLECTION)
                .whereIn("estado", Arrays.asList(Partido.ESTADO_ACTIVO, Partido.ESTADO_COMPLETO))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Partido> misPartidos = new ArrayList<>();
                    List<String> partidosPasados = new ArrayList<>();

                    queryDocumentSnapshots.getDocuments().forEach(documentSnapshot -> {
                        Partido partido = Partido.fromDocument(documentSnapshot);
                        if (partido == null) {
                            return;
                        }
                        if (!esPartidoDelUsuario(partido, usuarioIdActual)) {
                            return;
                        }

                        if (PartidoFechaHoraUtils.esFuturoOPresente(partido.getFecha(), partido.getHora())) {
                            misPartidos.add(partido);
                            return;
                        }

                        if (!partido.getIdPartido().isEmpty()) {
                            partidosPasados.add(partido.getIdPartido());
                        }
                    });

                    misPartidos.sort(
                            Comparator.comparing(
                                    partido -> PartidoFechaHoraUtils.parsearFechaHora(
                                            partido.getFecha(),
                                            partido.getHora()
                                    ),
                                    Comparator.nullsLast(Comparator.naturalOrder())
                            )
                    );

                    callback.onSuccess(misPartidos);
                    marcarPartidosPasadosComoFinalizados(partidosPasados);
                })
                .addOnFailureListener(exception ->
                        callback.onError(FirebaseAuthUtil.getAuthErrorMessage(context, exception))
                );
    }

    public void obtenerHistorialUsuarioAutenticado(
            @NonNull Context context,
            @NonNull ObtenerPartidosCallback callback
    ) {
        if (!authService.isUsuarioAutenticado()) {
            callback.onError(context.getString(R.string.msg_auth_invalid_credentials));
            return;
        }

        String usuarioIdActual = authService.getUsuarioIdActual();
        if (isBlank(usuarioIdActual)) {
            callback.onError(context.getString(R.string.msg_auth_invalid_credentials));
            return;
        }

        firestore
                .collection(PARTIDOS_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Partido> historial = new ArrayList<>();

                    queryDocumentSnapshots.getDocuments().forEach(documentSnapshot -> {
                        Partido partido = Partido.fromDocument(documentSnapshot);
                        if (partido == null) {
                            return;
                        }
                        if (!partido.participaUsuario(usuarioIdActual)) {
                            return;
                        }
                        if (!debeMostrarseEnHistorial(partido)) {
                            return;
                        }
                        historial.add(partido);
                    });

                    historial.sort(
                            Comparator.comparing(
                                    (Partido partido) -> PartidoFechaHoraUtils.parsearFechaHora(
                                            partido.getFecha(),
                                            partido.getHora()
                                    ),
                                    Comparator.nullsLast(Comparator.reverseOrder())
                            ).thenComparing(
                                    (Partido partido) -> partido.getFechaCreacion(),
                                    Comparator.nullsLast(Comparator.reverseOrder())
                            )
                    );

                    callback.onSuccess(historial);
                })
                .addOnFailureListener(exception ->
                        callback.onError(FirebaseAuthUtil.getAuthErrorMessage(context, exception))
                );
    }

    public void obtenerProximosPartidosActivos(
            @NonNull Context context,
            int limit,
            @NonNull ObtenerPartidosCallback callback
    ) {
        obtenerPartidosActivosYFuturos(context, new ObtenerPartidosCallback() {
            @Override
            public void onSuccess(@NonNull List<Partido> partidos) {
                if (limit <= 0 || partidos.size() <= limit) {
                    callback.onSuccess(partidos);
                    return;
                }
                callback.onSuccess(new ArrayList<>(partidos.subList(0, limit)));
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    @NonNull
    public List<Partido> filtrarPartidosActivosYFuturosPorFecha(
            @NonNull List<Partido> partidos,
            @Nullable String fechaFiltro
    ) {
        String fechaNormalizada = PartidoFechaHoraUtils.normalizarFecha(fechaFiltro);
        boolean usarFiltroFecha = !fechaNormalizada.isEmpty();
        List<Partido> partidosFiltrados = new ArrayList<>();

        for (Partido partido : partidos) {
            if (!PartidoFechaHoraUtils.esFuturoOPresente(partido.getFecha(), partido.getHora())) {
                continue;
            }
            if (usarFiltroFecha) {
                String fechaPartido = PartidoFechaHoraUtils.normalizarFecha(partido.getFecha());
                if (!fechaNormalizada.equals(fechaPartido)) {
                    continue;
                }
            }
            partidosFiltrados.add(partido);
        }

        partidosFiltrados.sort(
                Comparator.comparing(
                        partido -> PartidoFechaHoraUtils.parsearFechaHora(
                                partido.getFecha(),
                                partido.getHora()
                        ),
                        Comparator.nullsLast(Comparator.naturalOrder())
                )
        );
        return partidosFiltrados;
    }

    public void obtenerPartidoPorId(
            @NonNull Context context,
            @NonNull String partidoId,
            @NonNull ObtenerPartidoCallback callback
    ) {
        if (isBlank(partidoId)) {
            callback.onError(context.getString(R.string.msg_partido_id_invalido));
            return;
        }

        firestore
                .collection(PARTIDOS_COLLECTION)
                .document(partidoId.trim())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Partido partido = Partido.fromDocument(documentSnapshot);
                    if (partido == null) {
                        callback.onError(context.getString(R.string.msg_partido_no_encontrado));
                        return;
                    }
                    callback.onSuccess(partido);
                })
                .addOnFailureListener(exception ->
                        callback.onError(FirebaseAuthUtil.getAuthErrorMessage(context, exception))
                );
    }

    public void unirseAPartido(
            @NonNull Context context,
            @NonNull String partidoId,
            @NonNull GestionInscripcionCallback callback
    ) {
        gestionarInscripcion(context, partidoId, true, callback);
    }

    public void desapuntarseDePartido(
            @NonNull Context context,
            @NonNull String partidoId,
            @NonNull GestionInscripcionCallback callback
    ) {
        gestionarInscripcion(context, partidoId, false, callback);
    }

    public void confirmarResultado(
            @NonNull Context context,
            @NonNull String partidoId,
            @NonNull Resultado resultado,
            @NonNull ConfirmarResultadoCallback callback
    ) {
        if (isBlank(partidoId)) {
            callback.onError(context.getString(R.string.msg_partido_id_invalido));
            return;
        }

        if (!authService.isUsuarioAutenticado()) {
            callback.onError(context.getString(R.string.msg_auth_invalid_credentials));
            return;
        }

        String usuarioIdActual = authService.getUsuarioIdActual();
        if (isBlank(usuarioIdActual)) {
            callback.onError(context.getString(R.string.msg_auth_invalid_credentials));
            return;
        }

        DocumentReference partidoRef = firestore
                .collection(PARTIDOS_COLLECTION)
                .document(partidoId.trim());

        firestore.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(partidoRef);
                    Partido partido = Partido.fromDocument(snapshot);
                    if (partido == null) {
                        throw new OperacionResultadoException(
                                context.getString(R.string.msg_partido_no_encontrado)
                        );
                    }

                    validarReglasConfirmacionResultado(
                            partido,
                            usuarioIdActual,
                            context.getString(R.string.msg_resultado_solo_participantes),
                            context.getString(R.string.msg_resultado_partido_no_jugado),
                            context.getString(R.string.msg_resultado_ya_confirmado)
                    );
                    validarResultadoPorSets(
                            resultado,
                            context.getString(R.string.msg_resultado_set_obligatorio),
                            context.getString(R.string.msg_resultado_set_invalido),
                            context.getString(R.string.msg_resultado_set3_obligatorio),
                            context.getString(R.string.msg_resultado_tiebreak_obligatorio),
                            context.getString(R.string.msg_resultado_tiebreak_invalido)
                    );

                    Date now = new Date();
                    Resultado resultadoConfirmado = new Resultado();
                    resultadoConfirmado.setIdResultado(partido.getIdPartido() + "_" + now.getTime());
                    resultadoConfirmado.setSet1(resultado.getSet1());
                    resultadoConfirmado.setSet2(resultado.getSet2());
                    resultadoConfirmado.setSet3(resultado.getSet3());
                    resultadoConfirmado.setTieBreakSet1(resultado.getTieBreakSet1());
                    resultadoConfirmado.setTieBreakSet2(resultado.getTieBreakSet2());
                    resultadoConfirmado.setTieBreakSet3(resultado.getTieBreakSet3());
                    resultadoConfirmado.actualizarMarcadorDesdeSets();
                    resultadoConfirmado.setConfirmado(true);

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("resultado", resultadoConfirmado.toMap());
                    updates.put("resultadoConfirmado", true);
                    updates.put("estado", Partido.ESTADO_FINALIZADO);
                    updates.put("ultimaActualizacion", now);
                    transaction.update(partidoRef, updates);

                    partido.setResultado(resultadoConfirmado);
                    partido.setResultadoConfirmado(true);
                    partido.setEstado(Partido.ESTADO_FINALIZADO);
                    partido.setUltimaActualizacion(now);
                    return partido;
                })
                .addOnSuccessListener(partidoActualizado -> callback.onSuccess(
                        partidoActualizado,
                        context.getString(R.string.msg_resultado_confirmado_ok)
                ))
                .addOnFailureListener(exception ->
                        callback.onError(obtenerMensajeOperacionResultado(exception, context))
                );
    }

    public void limpiarPartidosInactivosAntiguos() {
        LocalDateTime fechaLimiteBorrado = LocalDateTime.now().minusDays(DIAS_LIMPIEZA_INACTIVOS);

        firestore.collection(PARTIDOS_COLLECTION)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> idsABorrar = new ArrayList<>();

                    queryDocumentSnapshots.getDocuments().forEach(documentSnapshot -> {
                        Partido partido = Partido.fromDocument(documentSnapshot);
                        if (partido == null) {
                            return;
                        }
                        if (debeBorrarsePorLimpieza(partido, fechaLimiteBorrado)
                                && !partido.getIdPartido().isEmpty()) {
                            idsABorrar.add(partido.getIdPartido());
                        }
                    });

                    borrarPartidos(idsABorrar);
                });
    }

    @NonNull
    @VisibleForTesting
    Partido construirPartido(
            @NonNull String deporte,
            @NonNull String fecha,
            @NonNull String hora,
            @NonNull String direccion,
            @NonNull String nivel,
            int personasIniciales,
            @NonNull String creadorId,
            @NonNull String creadorNombre
    ) {
        String deporteNormalizado = ValidadorPartido.normalizarDeporte(deporte);
        int personasInicialesNormalizadas = Partido.DEPORTE_PADEL.equals(deporteNormalizado)
                ? personasIniciales
                : 1;
        int acompanantesNormalizados = Partido.DEPORTE_PADEL.equals(deporteNormalizado)
                ? Math.max(personasInicialesNormalizadas - 1, 0)
                : 0;

        Partido partido = new Partido();
        partido.setDeporte(deporteNormalizado);
        partido.setFecha(PartidoFechaHoraUtils.normalizarFecha(fecha));
        partido.setHora(PartidoFechaHoraUtils.normalizarHora(hora));
        partido.setDireccion(ValidadorPartido.limpiarTexto(direccion));
        partido.setNivel(ValidadorPartido.limpiarTexto(nivel));
        partido.setCreadorId(creadorId);
        partido.setCreadorNombre(creadorNombre);
        partido.setMaxJugadores(ValidadorPartido.obtenerMaxJugadores(deporteNormalizado));
        partido.setAcompanantesIniciales(Math.max(acompanantesNormalizados, 0));
        partido.setPlazasOcupadas(
                ValidadorPartido.calcularPlazasOcupadasIniciales(
                        deporteNormalizado,
                        personasInicialesNormalizadas
                )
        );

        List<String> participantes = new ArrayList<>();
        participantes.add(creadorId);
        partido.setParticipantes(participantes);
        partido.setEstado(Partido.ESTADO_ACTIVO);
        partido.setResultado(null);
        partido.setResultadoConfirmado(false);
        return partido;
    }

    @NonNull
    @VisibleForTesting
    String obtenerNombreCreador(@NonNull Context context, @NonNull Usuario usuario) {
        String nombreCreador = usuario.getNombreMostrado();
        if (!nombreCreador.isEmpty()) {
            return nombreCreador;
        }
        return context.getString(R.string.home_nombre_no_disponible);
    }

    private void marcarPartidosPasadosComoFinalizados(@NonNull List<String> partidosIds) {
        if (partidosIds.isEmpty()) {
            return;
        }

        Date now = new Date();
        for (String partidoId : partidosIds) {
            if (partidoId == null || partidoId.trim().isEmpty()) {
                continue;
            }
            firestore
                    .collection(PARTIDOS_COLLECTION)
                    .document(partidoId.trim())
                    .update(
                            "estado", Partido.ESTADO_FINALIZADO,
                            "ultimaActualizacion", now
                    );
        }
    }

    @VisibleForTesting
    boolean esEstadoNoActivoValidoParaLimpieza(@NonNull String estado) {
        return Partido.ESTADO_FINALIZADO.equals(estado)
                || Partido.ESTADO_CANCELADO.equals(estado)
                || Partido.ESTADO_COMPLETO.equals(estado);
    }

    @VisibleForTesting
    boolean debeBorrarsePorLimpieza(
            @NonNull Partido partido,
            @NonNull LocalDateTime fechaLimiteBorrado
    ) {
        if (Partido.ESTADO_ACTIVO.equals(partido.getEstado())) {
            return false;
        }
        if (!esEstadoNoActivoValidoParaLimpieza(partido.getEstado())) {
            return false;
        }

        LocalDateTime fechaHoraPartido = PartidoFechaHoraUtils.parsearFechaHora(
                partido.getFecha(),
                partido.getHora()
        );
        if (fechaHoraPartido == null) {
            return false;
        }

        return !fechaHoraPartido.isAfter(fechaLimiteBorrado);
    }

    @VisibleForTesting
    boolean debeMostrarseEnListado(@NonNull Partido partido, @NonNull String usuarioIdActual) {
        if (Partido.ESTADO_ACTIVO.equals(partido.getEstado())) {
            return true;
        }
        if (!Partido.ESTADO_COMPLETO.equals(partido.getEstado())) {
            return false;
        }
        if (isBlank(usuarioIdActual)) {
            return false;
        }
        return partido.getParticipantes().contains(usuarioIdActual);
    }

    @VisibleForTesting
    boolean esPartidoDelUsuario(@NonNull Partido partido, @NonNull String usuarioIdActual) {
        if (isBlank(usuarioIdActual)) {
            return false;
        }
        return partido.participaUsuario(usuarioIdActual);
    }

    @VisibleForTesting
    boolean debeMostrarseEnHistorial(@NonNull Partido partido) {
        return partido.tieneResultadoConfirmado() || esPendienteResultado(partido);
    }

    @VisibleForTesting
    boolean esPendienteResultado(@NonNull Partido partido) {
        if (partido.tieneResultadoConfirmado()) {
            return false;
        }
        if (!noEstaCancelado(partido)) {
            return false;
        }
        if (!partido.estaCompleto()) {
            return false;
        }
        return haPasadoFechaHoraPartido(partido);
    }

    @VisibleForTesting
    boolean haPasadoFechaHoraPartido(@NonNull Partido partido) {
        LocalDateTime fechaHoraPartido = PartidoFechaHoraUtils.parsearFechaHora(
                partido.getFecha(),
                partido.getHora()
        );
        if (fechaHoraPartido == null) {
            return false;
        }
        return fechaHoraPartido.isBefore(LocalDateTime.now());
    }

    @VisibleForTesting
    boolean noEstaCancelado(@NonNull Partido partido) {
        return !Partido.ESTADO_CANCELADO.equals(partido.getEstado());
    }

    @VisibleForTesting
    void validarResultadoPorSets(
            @NonNull Resultado resultado,
            @NonNull String mensajeSetObligatorio,
            @NonNull String mensajeSetInvalido,
            @NonNull String mensajeSet3Obligatorio,
            @NonNull String mensajeTieBreakObligatorio,
            @NonNull String mensajeTieBreakInvalido
    ) {
        String set1 = normalizarTexto(resultado.getSet1());
        String set2 = normalizarTexto(resultado.getSet2());
        String set3 = normalizarTexto(resultado.getSet3());

        if (set1.isEmpty() || set2.isEmpty()) {
            throw new OperacionResultadoException(mensajeSetObligatorio);
        }
        if (!esSetValido(set1) || !esSetValido(set2)) {
            throw new OperacionResultadoException(mensajeSetInvalido);
        }

        int ganadorSet1 = obtenerGanadorSet(set1);
        int ganadorSet2 = obtenerGanadorSet(set2);
        boolean requiereSet3 = ganadorSet1 != ganadorSet2;

        if (requiereSet3) {
            if (set3.isEmpty()) {
                throw new OperacionResultadoException(mensajeSet3Obligatorio);
            }
            if (!esSetValido(set3)) {
                throw new OperacionResultadoException(mensajeSetInvalido);
            }
        } else {
            set3 = "";
            resultado.setSet3("");
        }

        validarTieBreakSet(
                set1,
                resultado.getTieBreakSet1(),
                mensajeTieBreakObligatorio,
                mensajeTieBreakInvalido
        );
        if (!requiereTieBreak(set1)) {
            resultado.setTieBreakSet1("");
        }
        validarTieBreakSet(
                set2,
                resultado.getTieBreakSet2(),
                mensajeTieBreakObligatorio,
                mensajeTieBreakInvalido
        );
        if (!requiereTieBreak(set2)) {
            resultado.setTieBreakSet2("");
        }
        if (requiereSet3) {
            validarTieBreakSet(
                    set3,
                    resultado.getTieBreakSet3(),
                    mensajeTieBreakObligatorio,
                    mensajeTieBreakInvalido
            );
        } else {
            resultado.setTieBreakSet3("");
        }
    }

    private void validarTieBreakSet(
            @NonNull String set,
            @Nullable String tieBreak,
            @NonNull String mensajeTieBreakObligatorio,
            @NonNull String mensajeTieBreakInvalido
    ) {
        if (!requiereTieBreak(set)) {
            return;
        }

        String tieBreakNormalizado = normalizarTexto(tieBreak);
        if (tieBreakNormalizado.isEmpty()) {
            throw new OperacionResultadoException(mensajeTieBreakObligatorio);
        }
        if (!esTieBreakValido(set, tieBreakNormalizado)) {
            throw new OperacionResultadoException(mensajeTieBreakInvalido);
        }
    }

    @VisibleForTesting
    boolean esSetValido(@Nullable String set) {
        String setNormalizado = normalizarTexto(set);
        return SETS_VALIDOS.contains(setNormalizado);
    }

    @VisibleForTesting
    boolean requiereTieBreak(@Nullable String set) {
        String setNormalizado = normalizarTexto(set);
        return "7-6".equals(setNormalizado) || "6-7".equals(setNormalizado);
    }

    @VisibleForTesting
    boolean esTieBreakValido(@Nullable String set, @Nullable String tieBreak) {
        String setNormalizado = normalizarTexto(set);
        String tieBreakNormalizado = normalizarTexto(tieBreak);
        int[] marcadorTieBreak = parsearMarcador(tieBreakNormalizado);
        if (marcadorTieBreak == null) {
            return false;
        }

        int puntosLocal = marcadorTieBreak[0];
        int puntosVisitante = marcadorTieBreak[1];
        int ganador = "7-6".equals(setNormalizado) ? puntosLocal : puntosVisitante;
        int perdedor = "7-6".equals(setNormalizado) ? puntosVisitante : puntosLocal;

        if (ganador < 7) {
            return false;
        }
        if (ganador <= perdedor) {
            return false;
        }
        return (ganador - perdedor) >= 2;
    }

    @VisibleForTesting
    int obtenerGanadorSet(@NonNull String set) {
        int[] marcador = parsearMarcador(set);
        if (marcador == null) {
            return 0;
        }
        if (marcador[0] > marcador[1]) {
            return 1;
        }
        if (marcador[1] > marcador[0]) {
            return 2;
        }
        return 0;
    }

    @Nullable
    private int[] parsearMarcador(@Nullable String marcador) {
        String valor = normalizarTexto(marcador);
        if (valor.isEmpty() || !valor.contains("-")) {
            return null;
        }
        String[] partes = valor.split("-");
        if (partes.length != 2) {
            return null;
        }
        try {
            int local = Integer.parseInt(partes[0].trim());
            int visitante = Integer.parseInt(partes[1].trim());
            if (local < 0 || visitante < 0) {
                return null;
            }
            return new int[]{local, visitante};
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @NonNull
    private String normalizarTexto(@Nullable String value) {
        return value == null ? "" : value.trim();
    }

    private void borrarPartidos(@NonNull List<String> idsABorrar) {
        if (idsABorrar.isEmpty()) {
            return;
        }

        WriteBatch batch = firestore.batch();
        for (String idPartido : idsABorrar) {
            if (idPartido == null || idPartido.trim().isEmpty()) {
                continue;
            }
            batch.delete(firestore.collection(PARTIDOS_COLLECTION).document(idPartido.trim()));
        }
        batch.commit();
    }

    private void gestionarInscripcion(
            @NonNull Context context,
            @NonNull String partidoId,
            boolean unirse,
            @NonNull GestionInscripcionCallback callback
    ) {
        if (isBlank(partidoId)) {
            callback.onError(context.getString(R.string.msg_partido_id_invalido));
            return;
        }

        if (!authService.isUsuarioAutenticado()) {
            callback.onError(context.getString(R.string.msg_auth_invalid_credentials));
            return;
        }

        String usuarioIdActual = authService.getUsuarioIdActual();
        if (isBlank(usuarioIdActual)) {
            callback.onError(context.getString(R.string.msg_auth_invalid_credentials));
            return;
        }

        DocumentReference partidoRef = firestore
                .collection(PARTIDOS_COLLECTION)
                .document(partidoId.trim());

        firestore.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(partidoRef);
                    Partido partido = Partido.fromDocument(snapshot);
                    if (partido == null) {
                        throw new OperacionInscripcionException(
                                context.getString(R.string.msg_partido_no_encontrado)
                        );
                    }

                    aplicarReglasInscripcion(
                            partido,
                            usuarioIdActual,
                            unirse,
                            context.getString(R.string.msg_partido_ya_apuntado),
                            context.getString(R.string.msg_partido_sin_plazas),
                            context.getString(R.string.msg_partido_operacion_error)
                    );
                    Date now = new Date();

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("participantes", partido.getParticipantes());
                    updates.put("plazasOcupadas", partido.getPlazasOcupadas());
                    updates.put("estado", partido.getEstado());
                    updates.put("ultimaActualizacion", now);
                    transaction.update(partidoRef, updates);

                    partido.setUltimaActualizacion(now);
                    return partido;
                })
                .addOnSuccessListener(partidoActualizado -> callback.onSuccess(
                        partidoActualizado,
                        context.getString(
                                unirse
                                        ? R.string.msg_partido_unido_ok
                                        : R.string.msg_partido_desapuntado_ok
                        )
                ))
                .addOnFailureListener(exception ->
                        callback.onError(obtenerMensajeOperacion(exception, context))
                );
    }

    @VisibleForTesting
    boolean esPartidoAbiertoParaInscripcion(@NonNull String estado) {
        return Partido.ESTADO_ACTIVO.equals(estado)
                || Partido.ESTADO_COMPLETO.equals(estado);
    }

    @VisibleForTesting
    void aplicarReglasInscripcion(
            @NonNull Partido partido,
            @NonNull String usuarioIdActual,
            boolean unirse,
            @NonNull String mensajeYaApuntado,
            @NonNull String mensajeSinPlazas,
            @NonNull String mensajeOperacionError
    ) {
        if (!esPartidoAbiertoParaInscripcion(partido.getEstado())) {
            throw new OperacionInscripcionException(mensajeOperacionError);
        }

        List<String> participantes = normalizarParticipantes(partido.getParticipantes());
        int maxJugadores = Math.max(partido.getMaxJugadores(), 0);
        int plazasOcupadas = Math.max(partido.getPlazasOcupadas(), 0);
        int plazasCalculadas = participantes.size() + Math.max(partido.getAcompanantesIniciales(), 0);
        plazasOcupadas = Math.max(plazasOcupadas, plazasCalculadas);
        if (maxJugadores > 0) {
            plazasOcupadas = Math.min(plazasOcupadas, maxJugadores);
        }

        boolean yaApuntado = participantes.contains(usuarioIdActual);
        if (unirse) {
            if (yaApuntado) {
                throw new OperacionInscripcionException(mensajeYaApuntado);
            }
            if (maxJugadores <= 0 || plazasOcupadas >= maxJugadores) {
                throw new OperacionInscripcionException(mensajeSinPlazas);
            }
            participantes.add(usuarioIdActual);
            plazasOcupadas = Math.min(plazasOcupadas + 1, maxJugadores);
        } else {
            if (!yaApuntado) {
                throw new OperacionInscripcionException(mensajeOperacionError);
            }
            participantes.remove(usuarioIdActual);
            plazasOcupadas = Math.max(plazasOcupadas - 1, 0);
        }

        String estadoActualizado = maxJugadores > 0 && plazasOcupadas >= maxJugadores
                ? Partido.ESTADO_COMPLETO
                : Partido.ESTADO_ACTIVO;

        partido.setParticipantes(participantes);
        partido.setPlazasOcupadas(plazasOcupadas);
        partido.setEstado(estadoActualizado);
    }

    @VisibleForTesting
    void validarReglasConfirmacionResultado(
            @NonNull Partido partido,
            @NonNull String usuarioIdActual,
            @NonNull String mensajeSoloParticipantes,
            @NonNull String mensajePartidoNoJugado,
            @NonNull String mensajeResultadoYaConfirmado
    ) {
        if (!partido.participaUsuario(usuarioIdActual)) {
            throw new OperacionResultadoException(mensajeSoloParticipantes);
        }
        if (partido.tieneResultadoConfirmado()) {
            throw new OperacionResultadoException(mensajeResultadoYaConfirmado);
        }
        if (!esPendienteResultado(partido)) {
            throw new OperacionResultadoException(mensajePartidoNoJugado);
        }
    }

    @NonNull
    @VisibleForTesting
    List<String> normalizarParticipantes(@NonNull List<String> participantesOriginales) {
        List<String> participantesNormalizados = new ArrayList<>();
        for (String participanteId : participantesOriginales) {
            if (participanteId == null) {
                continue;
            }
            String safeId = participanteId.trim();
            if (safeId.isEmpty() || participantesNormalizados.contains(safeId)) {
                continue;
            }
            participantesNormalizados.add(safeId);
        }
        return participantesNormalizados;
    }

    @NonNull
    @VisibleForTesting
    String obtenerMensajeOperacion(@NonNull Exception exception, @NonNull Context context) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof OperacionInscripcionException
                    && !isBlank(cause.getMessage())) {
                return cause.getMessage();
            }
            cause = cause.getCause();
        }
        return context.getString(R.string.msg_partido_operacion_error);
    }

    @NonNull
    private String obtenerMensajeOperacionResultado(
            @NonNull Exception exception,
            @NonNull Context context
    ) {
        Throwable cause = exception;
        while (cause != null) {
            if (cause instanceof OperacionResultadoException
                    && !isBlank(cause.getMessage())) {
                return cause.getMessage();
            }
            cause = cause.getCause();
        }
        return context.getString(R.string.msg_resultado_operacion_error);
    }

    private boolean isBlank(@Nullable String value) {
        return value == null || value.trim().isEmpty();
    }

    private static class OperacionInscripcionException extends RuntimeException {

        OperacionInscripcionException(@NonNull String message) {
            super(message);
        }
    }

    private static class OperacionResultadoException extends RuntimeException {

        OperacionResultadoException(@NonNull String message) {
            super(message);
        }
    }
}
