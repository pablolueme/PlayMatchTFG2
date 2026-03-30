package com.example.proyectofinaltfg2.ui;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.widget.Button;
import android.widget.EditText;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.repository.PartidoRepository;
import com.example.proyectofinaltfg2.repository.UsuarioRepository;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowToast;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RunWith(RobolectricTestRunner.class)
@Ignore("Desactivado temporalmente por inestabilidad con Coverage en Android Studio")
public class CrearPartidoActivityRobolectricTest {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Before
    public void limpiarToasts() {
        ShadowToast.reset();
    }

    @Test
    public void crearPartido_fechaVacia_muestraErrorYNoContinua() {
        CrearPartidoActivityTestable activity = Robolectric.buildActivity(CrearPartidoActivityTestable.class)
                .setup()
                .get();

        EditText edtHora = activity.findViewById(R.id.edt_hora_crear_partido);
        EditText edtDireccion = activity.findViewById(R.id.edt_direccion_crear_partido);
        EditText edtFecha = activity.findViewById(R.id.edt_fecha_crear_partido);
        Button btnCrear = activity.findViewById(R.id.btn_crear_partido);

        edtHora.setText("10:00");
        edtDireccion.setText("Calle Mayor 1");

        btnCrear.performClick();

        assertEquals(activity.getString(R.string.error_partido_fecha_required), String.valueOf(edtFecha.getError()));
        assertEquals(
                activity.getString(R.string.error_partido_fecha_hora_invalida_simple),
                ShadowToast.getTextOfLatestToast()
        );
        verify(activity.partidoRepositoryMock, never()).crearPartido(
                any(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyInt(),
                any()
        );
    }

    @Test
    public void crearPartido_horaInvalida_muestraErrorYNoContinua() {
        CrearPartidoActivityTestable activity = Robolectric.buildActivity(CrearPartidoActivityTestable.class)
                .setup()
                .get();

        EditText edtFecha = activity.findViewById(R.id.edt_fecha_crear_partido);
        EditText edtHora = activity.findViewById(R.id.edt_hora_crear_partido);
        EditText edtDireccion = activity.findViewById(R.id.edt_direccion_crear_partido);
        Button btnCrear = activity.findViewById(R.id.btn_crear_partido);

        edtFecha.setText(LocalDate.now().plusDays(2).format(FORMATO_FECHA));
        edtHora.setText("99:99");
        edtDireccion.setText("Calle Mayor 1");

        btnCrear.performClick();

        assertEquals(activity.getString(R.string.error_partido_hora_invalid), String.valueOf(edtHora.getError()));
        assertEquals(
                activity.getString(R.string.error_partido_fecha_hora_invalida_simple),
                ShadowToast.getTextOfLatestToast()
        );
        verify(activity.partidoRepositoryMock, never()).crearPartido(
                any(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyInt(),
                any()
        );
    }

    @Test
    public void crearPartido_direccionVacia_muestraErrorEnCampoYNoContinua() {
        CrearPartidoActivityTestable activity = Robolectric.buildActivity(CrearPartidoActivityTestable.class)
                .setup()
                .get();

        EditText edtFecha = activity.findViewById(R.id.edt_fecha_crear_partido);
        EditText edtHora = activity.findViewById(R.id.edt_hora_crear_partido);
        EditText edtDireccion = activity.findViewById(R.id.edt_direccion_crear_partido);
        Button btnCrear = activity.findViewById(R.id.btn_crear_partido);

        edtFecha.setText(LocalDate.now().plusDays(2).format(FORMATO_FECHA));
        edtHora.setText("10:00");
        edtDireccion.setText(" ");

        btnCrear.performClick();

        assertEquals(
                activity.getString(R.string.error_partido_direccion_required),
                String.valueOf(edtDireccion.getError())
        );
        verify(activity.partidoRepositoryMock, never()).crearPartido(
                any(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyString(),
                anyInt(),
                any()
        );
    }

    public static class CrearPartidoActivityTestable extends CrearPartidoActivity {
        final PartidoRepository partidoRepositoryMock = mock(PartidoRepository.class);
        final UsuarioRepository usuarioRepositoryMock = mock(UsuarioRepository.class);

        @Override
        protected PartidoRepository crearPartidoRepository() {
            return partidoRepositoryMock;
        }

        @Override
        protected UsuarioRepository crearUsuarioRepository() {
            return usuarioRepositoryMock;
        }
    }
}
