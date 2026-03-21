package com.example.proyectofinaltfg2.logic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class PartidoFechaHoraUtils {

    private static final List<DateTimeFormatter> FORMATOS_FECHA = Arrays.asList(
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()),
            DateTimeFormatter.ofPattern("d/M/yyyy", Locale.getDefault())
    );
    private static final List<DateTimeFormatter> FORMATOS_HORA = Arrays.asList(
            DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()),
            DateTimeFormatter.ofPattern("H:mm", Locale.getDefault())
    );
    private static final DateTimeFormatter FORMATO_SALIDA_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault());
    private static final DateTimeFormatter FORMATO_SALIDA_HORA =
            DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault());

    private PartidoFechaHoraUtils() {
        // Clase de utilidades
    }

    @Nullable
    public static LocalDate parsearFecha(@Nullable String fechaTexto) {
        String fecha = limpiarTexto(fechaTexto);
        if (fecha.isEmpty()) {
            return null;
        }

        for (DateTimeFormatter formato : FORMATOS_FECHA) {
            try {
                return LocalDate.parse(fecha, formato);
            } catch (DateTimeParseException ignored) {
                // Prueba con el siguiente formato.
            }
        }
        return null;
    }

    @Nullable
    public static LocalTime parsearHora(@Nullable String horaTexto) {
        String hora = limpiarTexto(horaTexto);
        if (hora.isEmpty()) {
            return null;
        }

        for (DateTimeFormatter formato : FORMATOS_HORA) {
            try {
                return LocalTime.parse(hora, formato);
            } catch (DateTimeParseException ignored) {
                // Prueba con el siguiente formato.
            }
        }
        return null;
    }

    @Nullable
    public static LocalDateTime parsearFechaHora(
            @Nullable String fechaTexto,
            @Nullable String horaTexto
    ) {
        LocalDate fecha = parsearFecha(fechaTexto);
        LocalTime hora = parsearHora(horaTexto);
        if (fecha == null || hora == null) {
            return null;
        }
        return LocalDateTime.of(fecha, hora);
    }

    public static boolean esFuturoOPresente(@Nullable String fechaTexto, @Nullable String horaTexto) {
        LocalDateTime fechaHora = parsearFechaHora(fechaTexto, horaTexto);
        if (fechaHora == null) {
            return false;
        }
        return !fechaHora.isBefore(LocalDateTime.now());
    }

    @NonNull
    public static String normalizarFecha(@Nullable String fechaTexto) {
        LocalDate fecha = parsearFecha(fechaTexto);
        if (fecha == null) {
            return limpiarTexto(fechaTexto);
        }
        return fecha.format(FORMATO_SALIDA_FECHA);
    }

    @NonNull
    public static String normalizarHora(@Nullable String horaTexto) {
        LocalTime hora = parsearHora(horaTexto);
        if (hora == null) {
            return limpiarTexto(horaTexto);
        }
        return hora.format(FORMATO_SALIDA_HORA);
    }

    @NonNull
    public static String formatearFechaHora(@Nullable String fechaTexto, @Nullable String horaTexto) {
        String fecha = normalizarFecha(fechaTexto);
        String hora = normalizarHora(horaTexto);
        if (fecha.isEmpty() && hora.isEmpty()) {
            return "";
        }
        if (fecha.isEmpty()) {
            return hora;
        }
        if (hora.isEmpty()) {
            return fecha;
        }
        return fecha + " - " + hora;
    }

    @NonNull
    private static String limpiarTexto(@Nullable String value) {
        return value == null ? "" : value.trim();
    }
}
