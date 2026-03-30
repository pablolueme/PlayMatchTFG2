package com.example.proyectofinaltfg2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PartidoTest {

    @Test
    public void constructor_porDefectoInicializaCamposSeguros() {
        Partido partido = new Partido();

        assertEquals("", partido.getIdPartido());
        assertEquals("", partido.getDeporte());
        assertEquals("", partido.getFecha());
        assertEquals("", partido.getHora());
        assertEquals("", partido.getDireccion());
        assertEquals("", partido.getNivel());
        assertEquals("", partido.getCreadorId());
        assertEquals("", partido.getCreadorNombre());
        assertTrue(partido.getParticipantes().isEmpty());
        assertEquals(0, partido.getAcompanantesIniciales());
        assertEquals(0, partido.getMaxJugadores());
        assertEquals(0, partido.getPlazasOcupadas());
        assertEquals(Partido.ESTADO_ACTIVO, partido.getEstado());
        assertNull(partido.getResultado());
        assertFalse(partido.isResultadoConfirmado());
        assertNull(partido.getFechaCreacion());
        assertNull(partido.getUltimaActualizacion());
    }

    @Test
    public void setParticipantes_eliminaDuplicadosYVacios() {
        Partido partido = new Partido();
        partido.setParticipantes(Arrays.asList("u1", "u2", "u1", " ", null, "u3"));

        List<String> participantes = partido.getParticipantes();
        assertEquals(3, participantes.size());
        assertEquals(Arrays.asList("u1", "u2", "u3"), participantes);
    }

    @Test
    public void getPlazasLibres_nuncaEsNegativo() {
        Partido partido = new Partido();
        partido.setMaxJugadores(2);
        partido.setPlazasOcupadas(4);

        assertEquals(0, partido.getPlazasLibres());
    }

    @Test
    public void getPlazasLibres_calculaCorrectamenteCuandoHayHuecos() {
        Partido partido = new Partido();
        partido.setMaxJugadores(4);
        partido.setPlazasOcupadas(1);

        assertEquals(3, partido.getPlazasLibres());
    }

    @Test
    public void setAcompanantesIniciales_noPermiteNegativos() {
        Partido partido = new Partido();
        partido.setAcompanantesIniciales(-3);

        assertEquals(0, partido.getAcompanantesIniciales());
    }

    @Test
    public void toMap_incluyeCamposPrincipales() {
        Partido partido = new Partido();
        partido.setIdPartido(" p1 ");
        partido.setDeporte(Partido.DEPORTE_PADEL);
        partido.setEstado(Partido.ESTADO_ACTIVO);
        partido.setParticipantes(Arrays.asList("u1", "u2"));
        partido.setMaxJugadores(4);
        partido.setPlazasOcupadas(2);

        Map<String, Object> map = partido.toMap();

        assertEquals("p1", map.get("idPartido"));
        assertEquals(Partido.DEPORTE_PADEL, map.get("deporte"));
        assertEquals(Partido.ESTADO_ACTIVO, map.get("estado"));
        assertEquals(4, map.get("maxJugadores"));
        assertEquals(2, map.get("plazasOcupadas"));
        assertTrue(((List<?>) map.get("participantes")).contains("u1"));
        assertFalse(((List<?>) map.get("participantes")).contains(" "));
    }

    @Test
    public void gettersSetters_saneanEspacios() {
        Partido partido = new Partido();
        partido.setIdPartido("  p1  ");
        partido.setDeporte("  padel ");
        partido.setFecha(" 01/04/2026 ");
        partido.setHora(" 09:00 ");
        partido.setDireccion(" Calle Mayor ");
        partido.setNivel(" 3 ");
        partido.setCreadorId(" u1 ");
        partido.setCreadorNombre(" Pablo ");
        partido.setEstado(" ACTIVO ");

        assertEquals("p1", partido.getIdPartido());
        assertEquals("padel", partido.getDeporte());
        assertEquals("01/04/2026", partido.getFecha());
        assertEquals("09:00", partido.getHora());
        assertEquals("Calle Mayor", partido.getDireccion());
        assertEquals("3", partido.getNivel());
        assertEquals("u1", partido.getCreadorId());
        assertEquals("Pablo", partido.getCreadorNombre());
        assertEquals("ACTIVO", partido.getEstado());
    }

    @Test
    public void setParticipantes_nullVaciaLista() {
        Partido partido = new Partido();
        partido.setParticipantes(null);

        assertTrue(partido.getParticipantes().isEmpty());
    }

    @Test
    public void getParticipantes_devuelveCopiaDefensiva() {
        Partido partido = new Partido();
        partido.setParticipantes(Arrays.asList("u1"));

        List<String> participantes = partido.getParticipantes();
        participantes.add("u2");

        assertEquals(1, partido.getParticipantes().size());
    }

    @Test
    public void toMap_incluyeResultadoSiExiste() {
        Partido partido = new Partido();
        partido.setResultado(new Resultado(" 6-4 6-4 "));

        Map<String, Object> map = partido.toMap();
        Object resultado = map.get("resultado");

        assertTrue(resultado instanceof Map);
        @SuppressWarnings("unchecked")
        Map<String, Object> resultadoMap = (Map<String, Object>) resultado;
        assertEquals("6-4 6-4", resultadoMap.get("marcador"));
    }
}
