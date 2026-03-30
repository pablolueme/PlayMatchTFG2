package com.example.proyectofinaltfg2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PartidoFromDocumentTest {

    @Test
    public void fromDocument_siNoExiste_devuelveNull() {
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        when(snapshot.exists()).thenReturn(false);

        assertNull(Partido.fromDocument(snapshot));
    }

    @Test
    public void fromDocument_mapeaCamposYFallbackId() {
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.getId()).thenReturn("doc-id");
        when(snapshot.getString("idPartido")).thenReturn(" ");
        when(snapshot.getString("deporte")).thenReturn(Partido.DEPORTE_PADEL);
        when(snapshot.getString("fecha")).thenReturn("01/04/2026");
        when(snapshot.getString("hora")).thenReturn("10:00");
        when(snapshot.getString("direccion")).thenReturn("Calle 1");
        when(snapshot.getString("nivel")).thenReturn("3");
        when(snapshot.getString("creadorId")).thenReturn("u1");
        when(snapshot.getString("creadorNombre")).thenReturn("Pablo");
        when(snapshot.get("participantes")).thenReturn(Arrays.asList("u1", " ", null, "u2", "u1"));
        when(snapshot.getLong("acompanantesIniciales")).thenReturn(0L);
        when(snapshot.getLong("maxJugadores")).thenReturn(4L);
        when(snapshot.getLong("plazasOcupadas")).thenReturn(3L);
        when(snapshot.getString("estado")).thenReturn(Partido.ESTADO_ACTIVO);
        when(snapshot.getBoolean("resultadoConfirmado")).thenReturn(true);

        Map<String, Object> resultadoRaw = new HashMap<>();
        resultadoRaw.put("marcador", "6-4 6-4");
        when(snapshot.get("resultado")).thenReturn(resultadoRaw);

        Partido partido = Partido.fromDocument(snapshot);

        assertNotNull(partido);
        assertEquals("doc-id", partido.getIdPartido());
        assertEquals(Partido.DEPORTE_PADEL, partido.getDeporte());
        assertEquals(2, partido.getParticipantes().size());
        assertEquals(2, partido.getAcompanantesIniciales());
        assertEquals(4, partido.getMaxJugadores());
        assertEquals(3, partido.getPlazasOcupadas());
        assertTrue(partido.isResultadoConfirmado());
        assertNotNull(partido.getResultado());
        assertEquals("6-4 6-4", partido.getResultado().getMarcador());
    }
}
