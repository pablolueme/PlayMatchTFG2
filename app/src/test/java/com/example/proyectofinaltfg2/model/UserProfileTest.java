package com.example.proyectofinaltfg2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Map;

public class UserProfileTest {

    @Test
    public void constructor_resuelveNivelDeportivoConPadel() {
        UserProfile profile = new UserProfile(
                "u1",
                "Pablo",
                "alias",
                "mail@test.com",
                "USER",
                4,
                2
        );

        assertEquals(4, profile.getNivelPadel());
        assertEquals(2, profile.getNivelTenis());
        assertEquals(4, profile.getNivelDeportivo());
    }

    @Test
    public void setNivelDeportivo_actualizaPadelYTenis() {
        UserProfile profile = new UserProfile();
        profile.setNivelDeportivo(3);

        assertEquals(3, profile.getNivelDeportivo());
        assertEquals(3, profile.getNivelPadel());
        assertEquals(3, profile.getNivelTenis());
    }

    @Test
    public void setNivelPadel_ySetNivelTenis_recalculanNivelDeportivo() {
        UserProfile profile = new UserProfile();
        profile.setNivelPadel(2);
        assertEquals(2, profile.getNivelDeportivo());

        profile.setNivelTenis(5);
        assertEquals(2, profile.getNivelDeportivo());
        assertEquals(5, profile.getNivelTenis());
    }

    @Test
    public void valoresPorDefecto_sonCoherentes() {
        UserProfile profile = new UserProfile();

        assertEquals(-1, profile.getNivelDeportivo());
        assertEquals(-1, profile.getNivelPadel());
        assertEquals(-1, profile.getNivelTenis());
        assertFalse(profile.isActivo());
    }

    @Test
    public void toFirestoreMap_incluyeAliasSoloSiNoVacio() {
        UserProfile sinAlias = new UserProfile();
        sinAlias.setUid("u1");
        Map<String, Object> mapSinAlias = sinAlias.toFirestoreMap();
        assertFalse(mapSinAlias.containsKey("alias"));

        UserProfile conAlias = new UserProfile();
        conAlias.setUid("u2");
        conAlias.setAlias("jugador2");
        Map<String, Object> mapConAlias = conAlias.toFirestoreMap();
        assertTrue(mapConAlias.containsKey("alias"));
        assertEquals("jugador2", mapConAlias.get("alias"));
    }

    @Test
    public void gettersCompatibilidad_replicanCamposPrincipales() {
        UserProfile profile = new UserProfile();
        profile.setNombre("Ana");
        profile.setCorreo("ana@test.com");
        profile.setRol("USER");

        assertEquals("Ana", profile.getNombreCompleto());
        assertEquals("ana@test.com", profile.getEmail());
        assertEquals("USER", profile.getRole());
    }

    @Test
    public void settersCompatibilidad_actualizanCamposPrincipales() {
        UserProfile profile = new UserProfile();
        profile.setNombreCompleto("Pablo");
        profile.setEmail("pablo@test.com");
        profile.setRole("CLUB");

        assertEquals("Pablo", profile.getNombre());
        assertEquals("pablo@test.com", profile.getCorreo());
        assertEquals("CLUB", profile.getRol());
    }

    @Test
    public void setters_saneanValoresNulosYEspacios() {
        UserProfile profile = new UserProfile();
        profile.setUid("  u1 ");
        profile.setNombre("  Ana ");
        profile.setAlias(null);
        profile.setCiudad("  Salamanca ");

        assertEquals("u1", profile.getUid());
        assertEquals("Ana", profile.getNombre());
        assertEquals("", profile.getAlias());
        assertEquals("Salamanca", profile.getCiudad());
    }
}
