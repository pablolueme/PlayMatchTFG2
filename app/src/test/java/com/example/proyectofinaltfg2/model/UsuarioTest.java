package com.example.proyectofinaltfg2.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UsuarioTest {

    @Test
    public void constructorPorDefecto_inicializaEnBlanco() {
        Usuario usuario = new Usuario();

        assertEquals("", usuario.getId());
        assertEquals("", usuario.getNombre());
        assertEquals("", usuario.getAlias());
        assertEquals("", usuario.getCorreo());
    }

    @Test
    public void constructorConParametros_saneaValores() {
        Usuario usuario = new Usuario("  id-1 ", " Ana ", " anita ", " ana@test.com ");

        assertEquals("id-1", usuario.getId());
        assertEquals("Ana", usuario.getNombre());
        assertEquals("anita", usuario.getAlias());
        assertEquals("ana@test.com", usuario.getCorreo());
    }

    @Test
    public void getNombreMostrado_priorizaAliasLuegoNombreLuegoCorreo() {
        Usuario usuario = new Usuario("u1", "Nombre", "Alias", "correo@test.com");
        assertEquals("Alias", usuario.getNombreMostrado());

        usuario.setAlias(" ");
        assertEquals("Nombre", usuario.getNombreMostrado());

        usuario.setNombre(" ");
        assertEquals("correo@test.com", usuario.getNombreMostrado());
    }

    @Test
    public void fromUserProfile_mapeaDatosBasicos() {
        UserProfile profile = new UserProfile();
        profile.setUid("id-1");
        profile.setNombre("Pablo");
        profile.setAlias("pabs");
        profile.setCorreo("pablo@test.com");

        Usuario usuario = Usuario.fromUserProfile(profile);

        assertEquals("id-1", usuario.getId());
        assertEquals("Pablo", usuario.getNombre());
        assertEquals("pabs", usuario.getAlias());
        assertEquals("pablo@test.com", usuario.getCorreo());
    }

    @Test
    public void setters_saneanEspaciosYNull() {
        Usuario usuario = new Usuario();
        usuario.setId("  id-2 ");
        usuario.setNombre("  Ana  ");
        usuario.setAlias(null);
        usuario.setCorreo("  a@test.com ");

        assertEquals("id-2", usuario.getId());
        assertEquals("Ana", usuario.getNombre());
        assertEquals("", usuario.getAlias());
        assertEquals("a@test.com", usuario.getCorreo());
    }
}
