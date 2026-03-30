package com.example.proyectofinaltfg2.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PartidoFechaHoraUtilsTest {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Test
    public void parsearFecha_formatoCorrecto_devuelveLocalDate() {
        LocalDate fecha = PartidoFechaHoraUtils.parsearFecha("01/04/2026");

        assertNotNull(fecha);
        assertEquals(LocalDate.of(2026, 4, 1), fecha);
    }

    @Test
    public void parsearFecha_formatoInvalido_devuelveNull() {
        assertNull(PartidoFechaHoraUtils.parsearFecha("2026-04-01"));
    }

    @Test
    public void parsearHora_formatoCorrecto_devuelveLocalTime() {
        LocalTime hora = PartidoFechaHoraUtils.parsearHora("9:05");

        assertNotNull(hora);
        assertEquals(LocalTime.of(9, 5), hora);
    }

    @Test
    public void parsearHora_formatoInvalido_devuelveNull() {
        assertNull(PartidoFechaHoraUtils.parsearHora("99:99"));
    }

    @Test
    public void parsearFechaHora_combinaFechaYHoraCorrectamente() {
        LocalDateTime fechaHora = PartidoFechaHoraUtils.parsearFechaHora("01/04/2026", "10:30");

        assertNotNull(fechaHora);
        assertEquals(LocalDateTime.of(2026, 4, 1, 10, 30), fechaHora);
    }

    @Test
    public void esFuturoOPresente_detectaPasadoYFuturo() {
        String fechaPasada = LocalDate.now().minusDays(1).format(FORMATO_FECHA);
        String fechaFutura = LocalDate.now().plusDays(2).format(FORMATO_FECHA);

        assertFalse(PartidoFechaHoraUtils.esFuturoOPresente(fechaPasada, "12:00"));
        assertTrue(PartidoFechaHoraUtils.esFuturoOPresente(fechaFutura, "12:00"));
    }

    @Test
    public void normalizarFechaYHora_devuelveFormatoEsperado() {
        assertEquals("01/04/2026", PartidoFechaHoraUtils.normalizarFecha("1/4/2026"));
        assertEquals("09:05", PartidoFechaHoraUtils.normalizarHora("9:05"));
    }

    @Test
    public void formatearFechaHora_componeTextoFinal() {
        assertEquals(
                "01/04/2026 - 09:05",
                PartidoFechaHoraUtils.formatearFechaHora("1/4/2026", "9:05")
        );
    }
}
