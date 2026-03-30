package com.example.proyectofinaltfg2.ui.fragments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.repository.PartidoRepository;
import com.example.proyectofinaltfg2.repository.UsuarioRepository;
import com.example.proyectofinaltfg2.ui.FragmentHostActivity;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
@Ignore("Desactivado temporalmente por inestabilidad con Coverage en Android Studio")
public class InicioFragmentRobolectricTest {

    @Test
    public void fragment_inicializaEstadoSinPartidosYSinBottomNav() {
        FragmentHostActivity activity = Robolectric.buildActivity(FragmentHostActivity.class)
                .create()
                .start()
                .get();

        InicioFragment fragment = new InicioFragmentTestable();
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(FragmentHostActivity.CONTAINER_ID, fragment)
                .commitNow();

        View root = fragment.getView();
        assertNotNull(root);

        View bottomNav = root.findViewById(R.id.include_bottom_nav_home);
        assertNotNull(bottomNav);
        assertEquals(View.GONE, bottomNav.getVisibility());

        TextView txtFecha = root.findViewById(R.id.txt_fecha_proximo_partido_home);
        TextView txtLugar = root.findViewById(R.id.txt_lugar_proximo_partido_home);
        Button btnVerPartido = root.findViewById(R.id.btn_ver_partido_home);
        View preview = root.findViewById(R.id.item_partido_preview_home);

        assertEquals(activity.getString(R.string.home_sin_partidos_fecha), txtFecha.getText().toString());
        assertEquals(activity.getString(R.string.home_sin_partidos_lugar), txtLugar.getText().toString());
        assertFalse(btnVerPartido.isEnabled());
        assertEquals(View.GONE, preview.getVisibility());
    }

    public static class InicioFragmentTestable extends InicioFragment {
        @Override
        protected UsuarioRepository crearUsuarioRepository() {
            return mock(UsuarioRepository.class);
        }

        @Override
        protected PartidoRepository crearPartidoRepository() {
            return mock(PartidoRepository.class);
        }
    }
}
