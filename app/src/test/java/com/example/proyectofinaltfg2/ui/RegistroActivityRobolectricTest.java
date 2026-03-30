package com.example.proyectofinaltfg2.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.proyectofinaltfg2.R;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
@Ignore("Desactivado temporalmente por inestabilidad con Coverage en Android Studio")
public class RegistroActivityRobolectricTest {

    @Test
    public void activity_arrancaEInicializaVistasClave() {
        RegistroActivity activity = Robolectric.buildActivity(RegistroActivity.class)
                .setup()
                .get();

        assertNotNull(activity.findViewById(R.id.edt_nombre_registro));
        assertNotNull(activity.findViewById(R.id.btn_selector_user));
        assertNotNull(activity.findViewById(R.id.btn_selector_club));
        assertNotNull(activity.findViewById(R.id.btn_crear_cuenta));
    }

    @Test
    public void selectorRol_muestraYOcultaCodigoClub() {
        RegistroActivity activity = Robolectric.buildActivity(RegistroActivity.class)
                .setup()
                .get();

        LinearLayout layoutCodigoClub = activity.findViewById(R.id.layout_codigo_club);
        Button btnUser = activity.findViewById(R.id.btn_selector_user);
        Button btnClub = activity.findViewById(R.id.btn_selector_club);

        assertEquals(View.GONE, layoutCodigoClub.getVisibility());

        btnClub.performClick();
        assertEquals(View.VISIBLE, layoutCodigoClub.getVisibility());

        btnUser.performClick();
        assertEquals(View.GONE, layoutCodigoClub.getVisibility());
    }

    @Test
    public void crearCuenta_conNombreVacio_muestraErrorEnNombre() {
        RegistroActivity activity = Robolectric.buildActivity(RegistroActivity.class)
                .setup()
                .get();

        EditText edtNombre = activity.findViewById(R.id.edt_nombre_registro);
        Button btnCrearCuenta = activity.findViewById(R.id.btn_crear_cuenta);
        ProgressBar progressRegistro = activity.findViewById(R.id.progress_registro);

        edtNombre.setText(" ");
        btnCrearCuenta.performClick();

        assertNotNull(edtNombre.getError());
        assertEquals(activity.getString(R.string.error_name_required), edtNombre.getError().toString());
        assertEquals(View.GONE, progressRegistro.getVisibility());
        assertTrue(btnCrearCuenta.isEnabled());
    }

    @Test
    public void crearCuenta_emailInvalido_muestraErrorEnCorreo() {
        RegistroActivity activity = Robolectric.buildActivity(RegistroActivity.class)
                .setup()
                .get();

        EditText edtNombre = activity.findViewById(R.id.edt_nombre_registro);
        EditText edtAlias = activity.findViewById(R.id.edt_alias_registro);
        EditText edtCorreo = activity.findViewById(R.id.edt_correo_registro);
        EditText edtPassword = activity.findViewById(R.id.edt_password_registro);
        EditText edtConfirmar = activity.findViewById(R.id.edt_confirmar_password_registro);
        AutoCompleteTextView nivelPadel = activity.findViewById(R.id.actv_nivel_padel);
        AutoCompleteTextView nivelTenis = activity.findViewById(R.id.actv_nivel_tenis);
        Button btnCrearCuenta = activity.findViewById(R.id.btn_crear_cuenta);

        edtNombre.setText("Ana");
        edtAlias.setText("ana");
        edtCorreo.setText("correo-invalido");
        edtPassword.setText("123456");
        edtConfirmar.setText("123456");
        nivelPadel.setText("3");
        nivelTenis.setText("3");

        btnCrearCuenta.performClick();

        assertNotNull(edtCorreo.getError());
        assertEquals(activity.getString(R.string.error_invalid_email_format), edtCorreo.getError().toString());
    }

    @Test
    public void crearCuenta_clubSinCodigo_muestraErrorEnCodigoClub() {
        RegistroActivity activity = Robolectric.buildActivity(RegistroActivity.class)
                .setup()
                .get();

        EditText edtNombre = activity.findViewById(R.id.edt_nombre_registro);
        EditText edtAlias = activity.findViewById(R.id.edt_alias_registro);
        EditText edtCorreo = activity.findViewById(R.id.edt_correo_registro);
        EditText edtPassword = activity.findViewById(R.id.edt_password_registro);
        EditText edtConfirmar = activity.findViewById(R.id.edt_confirmar_password_registro);
        EditText edtCodigoClub = activity.findViewById(R.id.edt_codigo_club);
        AutoCompleteTextView nivelPadel = activity.findViewById(R.id.actv_nivel_padel);
        AutoCompleteTextView nivelTenis = activity.findViewById(R.id.actv_nivel_tenis);
        Button btnSelectorClub = activity.findViewById(R.id.btn_selector_club);
        Button btnCrearCuenta = activity.findViewById(R.id.btn_crear_cuenta);

        btnSelectorClub.performClick();
        edtNombre.setText("Ana");
        edtAlias.setText("ana");
        edtCorreo.setText("ana@test.com");
        edtPassword.setText("123456");
        edtConfirmar.setText("123456");
        edtCodigoClub.setText(" ");
        nivelPadel.setText("3");
        nivelTenis.setText("3");

        btnCrearCuenta.performClick();

        assertNotNull(edtCodigoClub.getError());
        assertEquals(activity.getString(R.string.error_club_code_required), edtCodigoClub.getError().toString());
    }

    @Test
    public void selectoresNivel_tienenOpcionesDisponibles() {
        RegistroActivity activity = Robolectric.buildActivity(RegistroActivity.class)
                .setup()
                .get();

        AutoCompleteTextView nivelPadel = activity.findViewById(R.id.actv_nivel_padel);
        AutoCompleteTextView nivelTenis = activity.findViewById(R.id.actv_nivel_tenis);

        assertNotNull(nivelPadel.getAdapter());
        assertNotNull(nivelTenis.getAdapter());
        assertTrue(nivelPadel.getAdapter().getCount() >= 5);
        assertTrue(nivelTenis.getAdapter().getCount() >= 5);
    }
}
