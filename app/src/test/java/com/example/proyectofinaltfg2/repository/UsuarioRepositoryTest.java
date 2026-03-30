package com.example.proyectofinaltfg2.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.model.UserProfile;
import com.google.firebase.auth.FirebaseUser;

import org.junit.Test;

public class UsuarioRepositoryTest {

    @Test
    public void normalizeUserProfile_rellenaCamposObligatorios() {
        UsuarioRepository repository = new UsuarioRepository();
        UserProfile profile = new UserProfile();
        profile.setCiudad("Madrid");

        FirebaseUser currentUser = mock(FirebaseUser.class);
        when(currentUser.getUid()).thenReturn("uid-1");
        when(currentUser.getEmail()).thenReturn("mail@test.com");

        boolean changed = repository.normalizeUserProfile(profile, currentUser);

        assertTrue(changed);
        assertEquals("uid-1", profile.getUid());
        assertEquals("mail@test.com", profile.getCorreo());
        assertEquals("USER", profile.getRol());
        assertEquals(UsuarioRepository.CIUDAD_FIJA, profile.getCiudad());
    }

    @Test
    public void normalizeUserProfile_siYaEstaNormalizado_noCambia() {
        UsuarioRepository repository = new UsuarioRepository();
        UserProfile profile = new UserProfile();
        profile.setUid("uid-1");
        profile.setCorreo("mail@test.com");
        profile.setRol("USER");
        profile.setCiudad(UsuarioRepository.CIUDAD_FIJA);

        FirebaseUser currentUser = mock(FirebaseUser.class);
        when(currentUser.getUid()).thenReturn("uid-2");
        when(currentUser.getEmail()).thenReturn("other@test.com");

        boolean changed = repository.normalizeUserProfile(profile, currentUser);

        assertFalse(changed);
        assertEquals("uid-1", profile.getUid());
    }

    @Test
    public void normalizeUserProfile_siSoloCiudadEsDistinta_corrigeCiudad() {
        UsuarioRepository repository = new UsuarioRepository();
        UserProfile profile = new UserProfile();
        profile.setUid("uid-1");
        profile.setCorreo("mail@test.com");
        profile.setRol("USER");
        profile.setCiudad("Madrid");

        FirebaseUser currentUser = mock(FirebaseUser.class);
        when(currentUser.getUid()).thenReturn("uid-1");
        when(currentUser.getEmail()).thenReturn("mail@test.com");

        boolean changed = repository.normalizeUserProfile(profile, currentUser);

        assertTrue(changed);
        assertEquals(UsuarioRepository.CIUDAD_FIJA, profile.getCiudad());
    }

    @Test
    public void normalizeUserProfile_camposConEspacios_seNormalizanComoVacios() {
        UsuarioRepository repository = new UsuarioRepository();
        UserProfile profile = new UserProfile();
        profile.setUid(" ");
        profile.setCorreo(" ");
        profile.setRol(" ");
        profile.setCiudad(" ");

        FirebaseUser currentUser = mock(FirebaseUser.class);
        when(currentUser.getUid()).thenReturn("uid-9");
        when(currentUser.getEmail()).thenReturn("mail9@test.com");

        boolean changed = repository.normalizeUserProfile(profile, currentUser);

        assertTrue(changed);
        assertEquals("uid-9", profile.getUid());
        assertEquals("mail9@test.com", profile.getCorreo());
        assertEquals("USER", profile.getRol());
        assertEquals(UsuarioRepository.CIUDAD_FIJA, profile.getCiudad());
    }

    @Test
    public void buildFallbackProfile_creaPerfilBasicoEnSalamanca() {
        UsuarioRepository repository = new UsuarioRepository();
        FirebaseUser currentUser = mock(FirebaseUser.class);
        when(currentUser.getUid()).thenReturn("uid-7");
        when(currentUser.getEmail()).thenReturn("mail7@test.com");

        UserProfile fallback = repository.buildFallbackProfile(currentUser);

        assertEquals("uid-7", fallback.getUid());
        assertEquals("mail7@test.com", fallback.getCorreo());
        assertEquals("USER", fallback.getRol());
        assertEquals(UsuarioRepository.CIUDAD_FIJA, fallback.getCiudad());
        assertTrue(fallback.isActivo());
    }

    @Test
    public void buildFallbackProfile_siEmailEsNull_loDejaVacio() {
        UsuarioRepository repository = new UsuarioRepository();
        FirebaseUser currentUser = mock(FirebaseUser.class);
        when(currentUser.getUid()).thenReturn("uid-8");
        when(currentUser.getEmail()).thenReturn(null);

        UserProfile fallback = repository.buildFallbackProfile(currentUser);

        assertEquals("uid-8", fallback.getUid());
        assertEquals("", fallback.getCorreo());
    }

    @Test
    public void actualizarNombreYNivelesDeportivos_nombreVacio_devuelveError() {
        UsuarioRepository repository = new UsuarioRepository();
        Context context = mockContext();
        CapturaCallback callback = new CapturaCallback();

        repository.actualizarNombreYNivelesDeportivos(context, "  ", 3, 3, callback);

        assertEquals("error_nombre", callback.error);
    }

    @Test
    public void actualizarNombreYNivelesDeportivos_nivelPadelInvalido_devuelveError() {
        UsuarioRepository repository = new UsuarioRepository();
        Context context = mockContext();
        CapturaCallback callback = new CapturaCallback();

        repository.actualizarNombreYNivelesDeportivos(context, "Pablo", 0, 3, callback);

        assertEquals("error_nivel_padel", callback.error);
    }

    @Test
    public void actualizarNombreYNivelesDeportivos_nivelTenisInvalido_devuelveError() {
        UsuarioRepository repository = new UsuarioRepository();
        Context context = mockContext();
        CapturaCallback callback = new CapturaCallback();

        repository.actualizarNombreYNivelesDeportivos(context, "Pablo", 3, 9, callback);

        assertEquals("error_nivel_tenis", callback.error);
    }

    @Test
    public void validadoresInternos_nombreYNivel() {
        UsuarioRepository repository = new UsuarioRepository();

        assertTrue(repository.isNombreValido("Ana"));
        assertFalse(repository.isNombreValido(null));
        assertFalse(repository.isNombreValido(" "));
        assertTrue(repository.isNivelValido(1));
        assertTrue(repository.isNivelValido(5));
        assertFalse(repository.isNivelValido(0));
        assertFalse(repository.isNivelValido(6));
    }

    @Test
    public void actualizarNombreYNivelDeportivo_nivelInvalido_devuelveError() {
        UsuarioRepository repository = new UsuarioRepository();
        Context context = mockContext();
        CapturaCallback callback = new CapturaCallback();

        repository.actualizarNombreYNivelDeportivo(context, "Pablo", 0, callback);

        assertEquals("error_nivel_padel", callback.error);
    }

    @Test
    public void actualizarNombreYNivelesDeportivos_conDatosValidos_llegaAOtenerUsuarioActual() {
        UsuarioRepositoryTestable repository = new UsuarioRepositoryTestable();
        Context context = mockContext();
        CapturaCallback callback = new CapturaCallback();

        repository.actualizarNombreYNivelesDeportivos(context, "Pablo", 3, 4, callback);

        assertTrue(repository.obtenerUsuarioActualInvocado);
        assertEquals("error_from_obtener", callback.error);
    }

    private Context mockContext() {
        Context context = mock(Context.class);
        when(context.getString(R.string.error_name_required)).thenReturn("error_nombre");
        when(context.getString(R.string.error_nivel_padel_required)).thenReturn("error_nivel_padel");
        when(context.getString(R.string.error_nivel_tenis_required)).thenReturn("error_nivel_tenis");
        return context;
    }

    private static class CapturaCallback implements UsuarioRepository.RepositoryCallback {
        private String error = null;

        @Override
        public void onSuccess() {
        }

        @Override
        public void onError(String errorMessage) {
            this.error = errorMessage;
        }
    }

    private static class UsuarioRepositoryTestable extends UsuarioRepository {
        private boolean obtenerUsuarioActualInvocado;

        @Override
        public void obtenerUsuarioActual(Context context, UsuarioResultCallback callback) {
            obtenerUsuarioActualInvocado = true;
            callback.onError("error_from_obtener");
        }
    }
}
