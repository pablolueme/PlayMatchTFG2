package com.example.proyectofinaltfg2.ui.fragments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.repository.PartidoRepository;
import com.example.proyectofinaltfg2.ui.FragmentHostActivity;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
@Ignore("Desactivado temporalmente por inestabilidad con Coverage en Android Studio")
public class PartidosFragmentRobolectricTest {

    @Test
    public void fragmentModoTodos_inicializaTextosFiltrosYBottomNav() {
        FragmentHostActivity activity = Robolectric.buildActivity(FragmentHostActivity.class)
                .create()
                .start()
                .get();

        PartidosFragment fragment = new PartidosFragmentTestable();
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(FragmentHostActivity.CONTAINER_ID, fragment)
                .commitNow();

        View root = fragment.getView();
        assertNotNull(root);

        TextView titulo = root.findViewById(R.id.txt_titulo_partidos);
        EditText buscar = root.findViewById(R.id.edt_buscar_partidos);
        View bottomNav = root.findViewById(R.id.include_bottom_nav_partidos);
        TextView chipTodos = root.findViewById(R.id.chip_filtro_todos_partidos);
        TextView chipPadel = root.findViewById(R.id.chip_filtro_padel_partidos);

        assertEquals(activity.getString(R.string.partidos_titulo), titulo.getText().toString());
        assertEquals(activity.getString(R.string.hint_buscar_partidos), buscar.getHint().toString());
        assertEquals(View.GONE, bottomNav.getVisibility());
        assertEquals(1f, chipTodos.getAlpha(), 0.01f);
        assertEquals(0.55f, chipPadel.getAlpha(), 0.01f);
    }

    @Test
    public void fragmentModoMisPartidos_muestraTextosDeMisPartidos() {
        FragmentHostActivity activity = Robolectric.buildActivity(FragmentHostActivity.class)
                .create()
                .start()
                .get();

        PartidosFragment fragment = new PartidosFragmentTestable();
        fragment.setArguments(PartidosFragment.newInstance(PartidosFragment.MODO_MIS_PARTIDOS).getArguments());
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(FragmentHostActivity.CONTAINER_ID, fragment)
                .commitNow();

        View root = fragment.getView();
        assertNotNull(root);

        TextView titulo = root.findViewById(R.id.txt_titulo_partidos);
        EditText buscar = root.findViewById(R.id.edt_buscar_partidos);

        assertEquals(activity.getString(R.string.mis_partidos_titulo), titulo.getText().toString());
        assertEquals(activity.getString(R.string.hint_buscar_mis_partidos), buscar.getHint().toString());
    }

    public static class PartidosFragmentTestable extends PartidosFragment {
        @Override
        protected PartidoRepository crearPartidoRepository() {
            return mock(PartidoRepository.class);
        }
    }
}
