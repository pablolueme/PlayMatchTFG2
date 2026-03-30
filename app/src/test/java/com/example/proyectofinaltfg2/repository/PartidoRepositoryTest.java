package com.example.proyectofinaltfg2.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.model.Partido;
import com.example.proyectofinaltfg2.model.Usuario;
import com.example.proyectofinaltfg2.service.AuthService;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class PartidoRepositoryTest {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String MSG_YA_APUNTADO = "ya";
    private static final String MSG_SIN_PLAZAS = "sin plazas";
    private static final String MSG_OPERACION = "operacion";

    @Test
    public void construirPartido_tenisNormalizaPlazasYParticipantes() {
        PartidoRepository repository = crearRepository();

        Partido partido = repository.construirPartido(
                Partido.DEPORTE_TENIS,
                "1/4/2026",
                "9:00",
                " Calle 1 ",
                " 3 ",
                2,
                "u1",
                "Pablo"
        );

        assertEquals(Partido.DEPORTE_TENIS, partido.getDeporte());
        assertEquals("01/04/2026", partido.getFecha());
        assertEquals("09:00", partido.getHora());
        assertEquals(2, partido.getMaxJugadores());
        assertEquals(1, partido.getPlazasOcupadas());
        assertEquals(0, partido.getAcompanantesIniciales());
        assertEquals(Partido.ESTADO_ACTIVO, partido.getEstado());
        assertEquals(Arrays.asList("u1"), partido.getParticipantes());
    }

    @Test
    public void construirPartido_padelCalculaAcompanantesYPlazas() {
        PartidoRepository repository = crearRepository();

        Partido partido = repository.construirPartido(
                Partido.DEPORTE_PADEL,
                "1/4/2026",
                "9:00",
                "Calle 2",
                "2",
                2,
                "u1",
                "Pablo"
        );

        assertEquals(4, partido.getMaxJugadores());
        assertEquals(2, partido.getPlazasOcupadas());
        assertEquals(1, partido.getAcompanantesIniciales());
    }

    @Test
    public void construirPartido_deporteDesconocido_normalizaAPadel() {
        PartidoRepository repository = crearRepository();

        Partido partido = repository.construirPartido(
                "basket",
                "1/4/2026",
                "9:00",
                "Calle 2",
                "2",
                2,
                "u1",
                "Pablo"
        );

        assertEquals(Partido.DEPORTE_PADEL, partido.getDeporte());
        assertEquals(4, partido.getMaxJugadores());
    }

    @Test
    public void filtrarPartidosActivosYFuturosPorFecha_filtraYOrdena() {
        PartidoRepository repository = crearRepository();
        Partido partidoTarde = crearPartidoFuturo(2, "20:00");
        Partido partidoManana = crearPartidoFuturo(1, "09:00");
        Partido partidoPasado = crearPartidoPasado();

        List<Partido> filtrados = repository.filtrarPartidosActivosYFuturosPorFecha(
                Arrays.asList(partidoTarde, partidoPasado, partidoManana),
                ""
        );

        assertEquals(2, filtrados.size());
        assertEquals(partidoManana.getFecha(), filtrados.get(0).getFecha());
        assertEquals("09:00", filtrados.get(0).getHora());
        assertEquals(partidoTarde.getFecha(), filtrados.get(1).getFecha());
    }

    @Test
    public void filtrarPartidosActivosYFuturosPorFecha_aplicaFiltroFecha() {
        PartidoRepository repository = crearRepository();
        Partido partidoFecha1 = crearPartidoConFecha(LocalDate.now().plusDays(2), "10:00");
        Partido partidoFecha2 = crearPartidoConFecha(LocalDate.now().plusDays(3), "10:00");

        List<Partido> filtrados = repository.filtrarPartidosActivosYFuturosPorFecha(
                Arrays.asList(partidoFecha1, partidoFecha2),
                partidoFecha1.getFecha()
        );

        assertEquals(1, filtrados.size());
        assertEquals(partidoFecha1.getFecha(), filtrados.get(0).getFecha());
    }

    @Test
    public void aplicarReglasInscripcion_unirseCompletaPartidoCuandoNoHayPlazas() {
        PartidoRepository repository = crearRepository();
        Partido partido = partidoConParticipantes(Partido.ESTADO_ACTIVO, 2, 1, 0, Arrays.asList("u1"));

        repository.aplicarReglasInscripcion(
                partido,
                "u2",
                true,
                MSG_YA_APUNTADO,
                MSG_SIN_PLAZAS,
                MSG_OPERACION
        );

        assertEquals(Arrays.asList("u1", "u2"), partido.getParticipantes());
        assertEquals(2, partido.getPlazasOcupadas());
        assertEquals(Partido.ESTADO_COMPLETO, partido.getEstado());
    }

    @Test
    public void aplicarReglasInscripcion_unirseDuplicadoLanzaError() {
        PartidoRepository repository = crearRepository();
        Partido partido = partidoConParticipantes(Partido.ESTADO_ACTIVO, 4, 2, 0, Arrays.asList("u1", "u2"));

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> repository.aplicarReglasInscripcion(
                        partido,
                        "u2",
                        true,
                        MSG_YA_APUNTADO,
                        MSG_SIN_PLAZAS,
                        MSG_OPERACION
                )
        );

        assertEquals(MSG_YA_APUNTADO, error.getMessage());
    }

    @Test
    public void aplicarReglasInscripcion_unirseSinPlazasLanzaError() {
        PartidoRepository repository = crearRepository();
        Partido partido = partidoConParticipantes(Partido.ESTADO_COMPLETO, 2, 2, 0, Arrays.asList("u1", "u2"));

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> repository.aplicarReglasInscripcion(
                        partido,
                        "u3",
                        true,
                        MSG_YA_APUNTADO,
                        MSG_SIN_PLAZAS,
                        MSG_OPERACION
                )
        );

        assertEquals(MSG_SIN_PLAZAS, error.getMessage());
    }

    @Test
    public void aplicarReglasInscripcion_partidoNoAbiertoLanzaError() {
        PartidoRepository repository = crearRepository();
        Partido partido = partidoConParticipantes(Partido.ESTADO_FINALIZADO, 2, 1, 0, Arrays.asList("u1"));

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> repository.aplicarReglasInscripcion(
                        partido,
                        "u2",
                        true,
                        MSG_YA_APUNTADO,
                        MSG_SIN_PLAZAS,
                        MSG_OPERACION
                )
        );

        assertEquals(MSG_OPERACION, error.getMessage());
    }

    @Test
    public void aplicarReglasInscripcion_recalculaPlazasConAcompanantes() {
        PartidoRepository repository = crearRepository();
        Partido partido = partidoConParticipantes(Partido.ESTADO_ACTIVO, 4, 1, 2, Arrays.asList("u1"));

        repository.aplicarReglasInscripcion(
                partido,
                "u2",
                true,
                MSG_YA_APUNTADO,
                MSG_SIN_PLAZAS,
                MSG_OPERACION
        );

        assertEquals(Partido.ESTADO_COMPLETO, partido.getEstado());
        assertEquals(4, partido.getPlazasOcupadas());
        assertEquals(Arrays.asList("u1", "u2"), partido.getParticipantes());
    }

    @Test
    public void aplicarReglasInscripcion_desapuntarseLiberaPlazaYVuelveActivo() {
        PartidoRepository repository = crearRepository();
        Partido partido = partidoConParticipantes(
                Partido.ESTADO_COMPLETO,
                2,
                2,
                0,
                Arrays.asList("u1", "u2")
        );

        repository.aplicarReglasInscripcion(
                partido,
                "u2",
                false,
                MSG_YA_APUNTADO,
                MSG_SIN_PLAZAS,
                MSG_OPERACION
        );

        assertEquals(Arrays.asList("u1"), partido.getParticipantes());
        assertEquals(1, partido.getPlazasOcupadas());
        assertEquals(Partido.ESTADO_ACTIVO, partido.getEstado());
    }

    @Test
    public void aplicarReglasInscripcion_desapuntarseSinEstarApuntadoLanzaError() {
        PartidoRepository repository = crearRepository();
        Partido partido = partidoConParticipantes(Partido.ESTADO_ACTIVO, 4, 1, 0, Arrays.asList("u1"));

        RuntimeException error = assertThrows(
                RuntimeException.class,
                () -> repository.aplicarReglasInscripcion(
                        partido,
                        "u3",
                        false,
                        MSG_YA_APUNTADO,
                        MSG_SIN_PLAZAS,
                        MSG_OPERACION
                )
        );

        assertEquals(MSG_OPERACION, error.getMessage());
    }

    @Test
    public void normalizarParticipantes_quitaNulosDuplicadosYVacios() {
        PartidoRepository repository = crearRepository();

        List<String> participantes = repository.normalizarParticipantes(
                Arrays.asList("u1", null, "u2", "u1", "  ", "u3")
        );

        assertEquals(Arrays.asList("u1", "u2", "u3"), participantes);
    }

    @Test
    public void estadoNoActivoValidoParaLimpieza_segunEstado() {
        PartidoRepository repository = crearRepository();

        assertTrue(repository.esEstadoNoActivoValidoParaLimpieza(Partido.ESTADO_FINALIZADO));
        assertTrue(repository.esEstadoNoActivoValidoParaLimpieza(Partido.ESTADO_COMPLETO));
        assertTrue(repository.esEstadoNoActivoValidoParaLimpieza(Partido.ESTADO_CANCELADO));
        assertFalse(repository.esEstadoNoActivoValidoParaLimpieza(Partido.ESTADO_ACTIVO));
    }

    @Test
    public void debeBorrarsePorLimpieza_partidoInactivoYAntiguo_devuelveTrue() {
        PartidoRepository repository = crearRepository();
        Partido partido = new Partido();
        partido.setEstado(Partido.ESTADO_FINALIZADO);
        partido.setFecha(LocalDate.now().minusDays(5).format(FORMATO_FECHA));
        partido.setHora("10:00");

        boolean borrar = repository.debeBorrarsePorLimpieza(
                partido,
                LocalDateTime.now().minusDays(2)
        );

        assertTrue(borrar);
    }

    @Test
    public void debeBorrarsePorLimpieza_partidoActivoOReciente_devuelveFalse() {
        PartidoRepository repository = crearRepository();
        Partido activo = new Partido();
        activo.setEstado(Partido.ESTADO_ACTIVO);
        activo.setFecha(LocalDate.now().minusDays(10).format(FORMATO_FECHA));
        activo.setHora("10:00");

        Partido reciente = new Partido();
        reciente.setEstado(Partido.ESTADO_FINALIZADO);
        reciente.setFecha(LocalDate.now().plusDays(1).format(FORMATO_FECHA));
        reciente.setHora("10:00");

        assertFalse(repository.debeBorrarsePorLimpieza(activo, LocalDateTime.now().minusDays(2)));
        assertFalse(repository.debeBorrarsePorLimpieza(reciente, LocalDateTime.now().minusDays(2)));
    }

    @Test
    public void debeMostrarseEnListado_yEsPartidoDelUsuario_aplicanReglas() {
        PartidoRepository repository = crearRepository();
        Partido partidoActivo = partidoConParticipantes(Partido.ESTADO_ACTIVO, 4, 1, 0, Arrays.asList("u1"));
        Partido partidoCompleto = partidoConParticipantes(Partido.ESTADO_COMPLETO, 2, 2, 0, Arrays.asList("u1", "u2"));
        partidoCompleto.setCreadorId("u1");

        assertTrue(repository.debeMostrarseEnListado(partidoActivo, ""));
        assertTrue(repository.debeMostrarseEnListado(partidoCompleto, "u2"));
        assertFalse(repository.debeMostrarseEnListado(partidoCompleto, "u3"));

        assertTrue(repository.esPartidoDelUsuario(partidoCompleto, "u1"));
        assertTrue(repository.esPartidoDelUsuario(partidoCompleto, "u2"));
        assertFalse(repository.esPartidoDelUsuario(partidoCompleto, "u4"));
        assertFalse(repository.esPartidoDelUsuario(partidoCompleto, " "));
    }

    @Test
    public void debeMostrarseEnListado_estadoNoValidoNoSeMuestra() {
        PartidoRepository repository = crearRepository();
        Partido partidoFinalizado = partidoConParticipantes(Partido.ESTADO_FINALIZADO, 2, 1, 0, Arrays.asList("u1"));

        assertFalse(repository.debeMostrarseEnListado(partidoFinalizado, "u1"));
    }

    @Test
    public void esPartidoAbiertoParaInscripcion_soloActivoYCompleto() {
        PartidoRepository repository = crearRepository();

        assertTrue(repository.esPartidoAbiertoParaInscripcion(Partido.ESTADO_ACTIVO));
        assertTrue(repository.esPartidoAbiertoParaInscripcion(Partido.ESTADO_COMPLETO));
        assertFalse(repository.esPartidoAbiertoParaInscripcion(Partido.ESTADO_FINALIZADO));
    }

    @Test
    public void obtenerNombreCreador_siNoHayNombreUsaFallback() {
        PartidoRepository repository = crearRepository();
        Context context = mock(Context.class);
        when(context.getString(R.string.home_nombre_no_disponible)).thenReturn("Jugador");

        Usuario usuarioSinDatos = new Usuario(" ", " ", " ", " ");
        String nombre = repository.obtenerNombreCreador(context, usuarioSinDatos);

        assertEquals("Jugador", nombre);
    }

    @Test
    public void obtenerMensajeOperacion_priorizaMensajeDeExcepcionCustom() {
        PartidoRepository repository = crearRepository();
        Context context = mock(Context.class);
        when(context.getString(R.string.msg_partido_operacion_error)).thenReturn("fallback");

        RuntimeException custom = crearOperacionInscripcionException("mensaje custom");
        Exception wrapped = new RuntimeException(new RuntimeException(custom));

        String mensaje = repository.obtenerMensajeOperacion(wrapped, context);
        assertNotNull(mensaje);
        assertEquals("mensaje custom", mensaje);
    }

    @Test
    public void obtenerMensajeOperacion_siNoHayCustomUsaFallback() {
        PartidoRepository repository = crearRepository();
        Context context = mock(Context.class);
        when(context.getString(R.string.msg_partido_operacion_error)).thenReturn("fallback");

        String mensaje = repository.obtenerMensajeOperacion(new RuntimeException("x"), context);

        assertEquals("fallback", mensaje);
    }

    private PartidoRepository crearRepository() {
        return new PartidoRepository(
                mock(FirebaseFirestore.class),
                mock(UsuarioRepository.class),
                mock(AuthService.class)
        );
    }

    private Partido crearPartidoFuturo(int diasEnFuturo, String hora) {
        return crearPartidoConFecha(LocalDate.now().plusDays(diasEnFuturo), hora);
    }

    private Partido crearPartidoPasado() {
        return crearPartidoConFecha(LocalDate.now().minusDays(1), "10:00");
    }

    private Partido crearPartidoConFecha(LocalDate fecha, String hora) {
        Partido partido = new Partido();
        partido.setFecha(fecha.format(FORMATO_FECHA));
        partido.setHora(hora);
        partido.setEstado(Partido.ESTADO_ACTIVO);
        return partido;
    }

    private Partido partidoConParticipantes(
            String estado,
            int maxJugadores,
            int plazasOcupadas,
            int acompanantesIniciales,
            List<String> participantes
    ) {
        Partido partido = new Partido();
        partido.setEstado(estado);
        partido.setMaxJugadores(maxJugadores);
        partido.setPlazasOcupadas(plazasOcupadas);
        partido.setAcompanantesIniciales(acompanantesIniciales);
        partido.setParticipantes(participantes);
        return partido;
    }

    private RuntimeException crearOperacionInscripcionException(String mensaje) {
        try {
            Class<?> clazz = Class.forName(
                    "com.example.proyectofinaltfg2.repository.PartidoRepository$OperacionInscripcionException"
            );
            java.lang.reflect.Constructor<?> constructor = clazz.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            return (RuntimeException) constructor.newInstance(mensaje);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
