package com.example.proyectofinaltfg2.ui.fragments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.view.View;
import android.widget.TextView;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.ui.FragmentHostActivity;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
@Ignore("Desactivado temporalmente por inestabilidad con Coverage en Android Studio")
public class EventosFragmentRobolectricTest {

    @Test
    public void fragment_infladoCorrectoYBottomNavOculto() {
        FragmentHostActivity activity = Robolectric.buildActivity(FragmentHostActivity.class)
                .create()
                .start()
                .get();

        EventosFragment fragment = new EventosFragment();
        activity.getSupportFragmentManager()
                .beginTransaction()
                .replace(FragmentHostActivity.CONTAINER_ID, fragment)
                .commitNow();

        View root = fragment.getView();
        assertNotNull(root);

        View bottomNav = root.findViewById(R.id.include_bottom_nav_eventos);
        TextView titulo = root.findViewById(R.id.txt_titulo_eventos);

        assertEquals(View.GONE, bottomNav.getVisibility());
        assertEquals(activity.getString(R.string.eventos_titulo), titulo.getText().toString());
    }
}
