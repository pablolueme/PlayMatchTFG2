package com.example.proyectofinaltfg2.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ResultadoTest {

    @Test
    public void toMap_yFromMap_mantienenCamposBasicos() {
        Resultado resultado = new Resultado();
        resultado.setIdResultado(" r1 ");
        resultado.setSet1("7-6");
        resultado.setSet2("6-4");
        resultado.setTieBreakSet1("7-5");
        resultado.actualizarMarcadorDesdeSets();
        resultado.setConfirmado(true);

        Map<String, Object> map = resultado.toMap();
        Resultado restaurado = Resultado.fromMap(map);

        assertEquals("r1", restaurado.getIdResultado());
        assertEquals("7-6 (7-5) 6-4", restaurado.getMarcador());
        assertEquals("7-6", restaurado.getSet1());
        assertEquals("6-4", restaurado.getSet2());
        assertEquals("7-5", restaurado.getTieBreakSet1());
        assertTrue(restaurado.isConfirmado());
    }

    @Test
    public void fromMap_siFaltanCampos_aplicaValoresSeguros() {
        Resultado restaurado = Resultado.fromMap(null);
        assertNull(restaurado);

        Resultado vacio = Resultado.fromMap(new HashMap<>());
        assertEquals("", vacio.getIdResultado());
        assertEquals("", vacio.getMarcador());
        assertEquals("", vacio.getSet1());
        assertFalse(vacio.isConfirmado());
    }
}
