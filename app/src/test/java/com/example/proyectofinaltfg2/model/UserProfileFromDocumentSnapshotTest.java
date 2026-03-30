package com.example.proyectofinaltfg2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Test;

public class UserProfileFromDocumentSnapshotTest {

    @Test
    public void fromDocumentSnapshot_siNoExiste_devuelveNull() {
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        when(snapshot.exists()).thenReturn(false);

        assertNull(UserProfile.fromDocumentSnapshot(snapshot));
    }

    @Test
    public void fromDocumentSnapshot_mapeaCamposConFallbackLegacy() {
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.getId()).thenReturn("uid-doc");

        when(snapshot.getString("uid")).thenReturn(" ");
        when(snapshot.getString("nombre")).thenReturn(null);
        when(snapshot.getString("nombreCompleto")).thenReturn(" Ana ");
        when(snapshot.getString("correo")).thenReturn(null);
        when(snapshot.getString("email")).thenReturn(" ana@test.com ");
        when(snapshot.getString("rol")).thenReturn(null);
        when(snapshot.getString("role")).thenReturn(" USER ");
        when(snapshot.getString("ciudad")).thenReturn(" Salamanca ");
        when(snapshot.getString("alias")).thenReturn(" ani ");

        when(snapshot.getLong("nivelDeportivo")).thenReturn(null);
        when(snapshot.getLong("nivelPadel")).thenReturn(4L);
        when(snapshot.getLong("nivelTenis")).thenReturn(2L);
        when(snapshot.getBoolean("activo")).thenReturn(null);

        UserProfile profile = UserProfile.fromDocumentSnapshot(snapshot);

        assertNotNull(profile);
        assertEquals("uid-doc", profile.getUid());
        assertEquals("Ana", profile.getNombre());
        assertEquals("ana@test.com", profile.getCorreo());
        assertEquals("USER", profile.getRol());
        assertEquals("Salamanca", profile.getCiudad());
        assertEquals(4, profile.getNivelPadel());
        assertEquals(2, profile.getNivelTenis());
        assertEquals("ani", profile.getAlias());
        assertTrue(profile.isActivo());
    }
}
