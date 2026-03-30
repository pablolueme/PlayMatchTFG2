package com.example.proyectofinaltfg2.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.firebase.auth.FirebaseUser;

import org.junit.Test;

public class AuthServiceTest {

    @Test
    public void isUsuarioAutenticado_delegaEnGateway() {
        AuthService.AuthGateway gateway = mock(AuthService.AuthGateway.class);
        when(gateway.isUserLoggedIn()).thenReturn(true);
        AuthService authService = new AuthService(gateway);

        assertTrue(authService.isUsuarioAutenticado());
    }

    @Test
    public void isUsuarioAutenticado_falseCuandoGatewayIndicaNoLogueado() {
        AuthService.AuthGateway gateway = mock(AuthService.AuthGateway.class);
        when(gateway.isUserLoggedIn()).thenReturn(false);
        AuthService authService = new AuthService(gateway);

        org.junit.Assert.assertFalse(authService.isUsuarioAutenticado());
    }

    @Test
    public void getUsuarioIdActual_devuelveVacioSiNoHayUsuario() {
        AuthService.AuthGateway gateway = mock(AuthService.AuthGateway.class);
        when(gateway.getCurrentUser()).thenReturn(null);
        AuthService authService = new AuthService(gateway);

        assertEquals("", authService.getUsuarioIdActual());
    }

    @Test
    public void getUsuarioIdActual_haceTrimDelUid() {
        AuthService.AuthGateway gateway = mock(AuthService.AuthGateway.class);
        FirebaseUser user = mock(FirebaseUser.class);
        when(user.getUid()).thenReturn("  uid-10  ");
        when(gateway.getCurrentUser()).thenReturn(user);
        AuthService authService = new AuthService(gateway);

        assertEquals("uid-10", authService.getUsuarioIdActual());
    }

    @Test
    public void getUsuarioIdActual_devuelveVacioSiUidEsNull() {
        AuthService.AuthGateway gateway = mock(AuthService.AuthGateway.class);
        FirebaseUser user = mock(FirebaseUser.class);
        when(user.getUid()).thenReturn(null);
        when(gateway.getCurrentUser()).thenReturn(user);
        AuthService authService = new AuthService(gateway);

        assertEquals("", authService.getUsuarioIdActual());
    }

    @Test
    public void getUsuarioActual_devuelveMismoUsuarioDelGateway() {
        AuthService.AuthGateway gateway = mock(AuthService.AuthGateway.class);
        FirebaseUser user = mock(FirebaseUser.class);
        when(gateway.getCurrentUser()).thenReturn(user);
        AuthService authService = new AuthService(gateway);

        assertSame(user, authService.getUsuarioActual());
    }

    @Test
    public void getUsuarioActual_nullSiGatewayNoTieneUsuario() {
        AuthService.AuthGateway gateway = mock(AuthService.AuthGateway.class);
        when(gateway.getCurrentUser()).thenReturn(null);
        AuthService authService = new AuthService(gateway);

        assertNull(authService.getUsuarioActual());
    }
}
