package com.example.proyectofinaltfg2.ui.fragments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.ui.FragmentHostActivity;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
@Ignore("Desactivado temporalmente por inestabilidad con Coverage en Android Studio")
public class PerfilFragmentRobolectricTest {

    @Test
    public void fragment_inicializaVistasYDeshabilitaSeccionesPendientes() {
        FragmentHostActivity activity = Robolectric.buildActivity(FragmentHostActivity.class)
                .create()
                .start()
                .get();

        PerfilFragment fragment = new PerfilFragment();
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(FragmentHostActivity.CONTAINER_ID, fragment)
                .commitNow();

        View root = fragment.getView();
        assertNotNull(root);

        View bottomNav = root.findViewById(R.id.include_bottom_nav_perfil);
        Button btnEditar = root.findViewById(R.id.btn_editar_perfil);
        Button btnCerrarSesion = root.findViewById(R.id.btn_cerrar_sesion_perfil);
        LinearLayout cardHistorial = root.findViewById(R.id.card_historial_perfil);
        LinearLayout cardAjustes = root.findViewById(R.id.card_ajustes_perfil);

        assertEquals(View.GONE, bottomNav.getVisibility());
        assertNotNull(btnEditar);
        assertNotNull(btnCerrarSesion);
        assertFalse(cardHistorial.isEnabled());
        assertFalse(cardHistorial.isClickable());
        assertFalse(cardAjustes.isEnabled());
        assertFalse(cardAjustes.isClickable());
    }
}
