package com.example.proyectofinaltfg2.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.model.Partido;

import org.junit.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ValidadorPartidoTest {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Test
    public void deporteValido_padelYTenis() {
        assertTrue(ValidadorPartido.esDeporteValido("padel"));
        assertTrue(ValidadorPartido.esDeporteValido("  tenis "));
        assertFalse(ValidadorPartido.esDeporteValido("futbol"));
    }

    @Test
    public void validarFormulario_deporteInvalido_devuelveErrorCampoDeporte() {
        ValidadorPartido.ResultadoValidacion resultado = validarFormulario(
                "futbol",
                fechaFutura(),
                "10:00",
                "Calle Mayor 1",
                "3",
                1
        );

        assertFalse(resultado.esValido());
        assertEquals(ValidadorPartido.Campo.DEPORTE, resultado.getCampo());
        assertEquals(R.string.error_partido_deporte_required, resultado.getMensajeResId());
    }

    @Test
    public void validarFormulario_fechaInvalida_devuelveErrorCampoFecha() {
        ValidadorPartido.ResultadoValidacion resultado = validarFormulario(
                Partido.DEPORTE_PADEL,
                "32/13/2026",
                "10:00",
                "Calle Mayor 1",
                "3",
                1
        );

        assertFalse(resultado.esValido());
        assertEquals(ValidadorPartido.Campo.FECHA, resultado.getCampo());
        assertEquals(R.string.error_partido_fecha_invalid, resultado.getMensajeResId());
    }

    @Test
    public void validarFormulario_horaInvalida_devuelveErrorCampoHora() {
        ValidadorPartido.ResultadoValidacion resultado = validarFormulario(
                Partido.DEPORTE_PADEL,
                fechaFutura(),
                "25:90",
                "Calle Mayor 1",
                "3",
                1
        );

        assertFalse(resultado.esValido());
        assertEquals(ValidadorPartido.Campo.HORA, resultado.getCampo());
        assertEquals(R.string.error_partido_hora_invalid, resultado.getMensajeResId());
    }

    @Test
    public void validarFormulario_direccionVacia_devuelveErrorCampoDireccion() {
        ValidadorPartido.ResultadoValidacion resultado = validarFormulario(
                Partido.DEPORTE_PADEL,
                fechaFutura(),
                "10:00",
                "   ",
                "3",
                1
        );

        assertFalse(resultado.esValido());
        assertEquals(ValidadorPartido.Campo.DIRECCION, resultado.getCampo());
        assertEquals(R.string.error_partido_direccion_required, resultado.getMensajeResId());
    }

    @Test
    public void validarFormulario_datosCorrectos_devuelveValido() {
        ValidadorPartido.ResultadoValidacion resultado = validarFormulario(
                Partido.DEPORTE_PADEL,
                fechaFutura(),
                "10:00",
                "Calle Mayor 1",
                "3",
                1
        );

        assertTrue(resultado.esValido());
    }

    @Test
    public void obtenerMaxJugadores_segunDeporte() {
        assertEquals(4, ValidadorPartido.obtenerMaxJugadores(Partido.DEPORTE_PADEL));
        assertEquals(2, ValidadorPartido.obtenerMaxJugadores(Partido.DEPORTE_TENIS));
    }

    @Test
    public void personasIniciales_seValidanSegunDeporte() {
        assertTrue(ValidadorPartido.esPersonasInicialesValido(Partido.DEPORTE_PADEL, 1));
        assertTrue(ValidadorPartido.esPersonasInicialesValido(Partido.DEPORTE_PADEL, 2));
        assertFalse(ValidadorPartido.esPersonasInicialesValido(Partido.DEPORTE_PADEL, 3));
        assertTrue(ValidadorPartido.esPersonasInicialesValido(Partido.DEPORTE_TENIS, 1));
        assertFalse(ValidadorPartido.esPersonasInicialesValido(Partido.DEPORTE_TENIS, 2));
    }

    @Test
    public void calcularPlazasIniciales_tenisSiempreUno_padelRespetaValor() {
        assertEquals(2, ValidadorPartido.calcularPlazasOcupadasIniciales(Partido.DEPORTE_PADEL, 2));
        assertEquals(1, ValidadorPartido.calcularPlazasOcupadasIniciales(Partido.DEPORTE_TENIS, 2));
    }

    private ValidadorPartido.ResultadoValidacion validarFormulario(
            String deporte,
            String fecha,
            String hora,
            String direccion,
            String nivel,
            int personasIniciales
    ) {
        return ValidadorPartido.validarFormularioCrear(
                deporte,
                fecha,
                hora,
                direccion,
                nivel,
                personasIniciales
        );
    }

    private String fechaFutura() {
        return LocalDate.now().plusDays(2).format(FORMATO_FECHA);
    }
}
