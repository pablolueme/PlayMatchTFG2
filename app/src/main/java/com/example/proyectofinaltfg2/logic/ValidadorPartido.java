package com.example.proyectofinaltfg2.logic;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.model.Partido;
import com.example.proyectofinaltfg2.utils.ValidationUtils;

import java.util.Locale;

public final class ValidadorPartido {

    public enum Campo {
        DEPORTE,
        FECHA,
        HORA,
        DIRECCION,
        NIVEL,
        PERSONAS_INICIALES
    }

    public static final class ResultadoValidacion {
        private final boolean valido;
        @StringRes
        private final int mensajeResId;
        @Nullable
        private final Campo campo;

        private ResultadoValidacion(
                boolean valido,
                @StringRes int mensajeResId,
                @Nullable Campo campo
        ) {
            this.valido = valido;
            this.mensajeResId = mensajeResId;
            this.campo = campo;
        }

        @NonNull
        public static ResultadoValidacion valido() {
            return new ResultadoValidacion(true, 0, null);
        }

        @NonNull
        public static ResultadoValidacion invalido(@StringRes int mensajeResId, @NonNull Campo campo) {
            return new ResultadoValidacion(false, mensajeResId, campo);
        }

        public boolean esValido() {
            return valido;
        }

        @StringRes
        public int getMensajeResId() {
            return mensajeResId;
        }

        @Nullable
        public Campo getCampo() {
            return campo;
        }
    }

    private ValidadorPartido() {
        // Clase de utilidades.
    }

    @NonNull
    public static ResultadoValidacion validarFormularioCrear(
            @Nullable String deporte,
            @Nullable String fecha,
            @Nullable String hora,
            @Nullable String direccion,
            @Nullable String nivel,
            int personasIniciales
    ) {
        if (!esDeporteValido(deporte)) {
            return ResultadoValidacion.invalido(R.string.error_partido_deporte_required, Campo.DEPORTE);
        }

        if (estaVacio(fecha)) {
            return ResultadoValidacion.invalido(R.string.error_partido_fecha_required, Campo.FECHA);
        }
        if (PartidoFechaHoraUtils.parsearFecha(fecha) == null) {
            return ResultadoValidacion.invalido(R.string.error_partido_fecha_invalid, Campo.FECHA);
        }

        if (estaVacio(hora)) {
            return ResultadoValidacion.invalido(R.string.error_partido_hora_required, Campo.HORA);
        }
        if (PartidoFechaHoraUtils.parsearHora(hora) == null) {
            return ResultadoValidacion.invalido(R.string.error_partido_hora_invalid, Campo.HORA);
        }

        if (estaVacio(direccion)) {
            return ResultadoValidacion.invalido(R.string.error_partido_direccion_required, Campo.DIRECCION);
        }

        if (estaVacio(nivel)) {
            return ResultadoValidacion.invalido(R.string.error_partido_nivel_required, Campo.NIVEL);
        }
        if (parsearNivel(nivel) == -1) {
            return ResultadoValidacion.invalido(R.string.error_profile_level_required, Campo.NIVEL);
        }

        if (!PartidoFechaHoraUtils.esFuturoOPresente(fecha, hora)) {
            return ResultadoValidacion.invalido(R.string.error_partido_fecha_hora_pasada, Campo.FECHA);
        }

        if (!esPersonasInicialesValido(deporte, personasIniciales)) {
            return ResultadoValidacion.invalido(
                    R.string.error_partido_acompanantes_invalid,
                    Campo.PERSONAS_INICIALES
            );
        }

        int plazasOcupadasIniciales = calcularPlazasOcupadasIniciales(deporte, personasIniciales);
        int maxJugadores = obtenerMaxJugadores(deporte);
        if (plazasOcupadasIniciales <= 0 || plazasOcupadasIniciales >= maxJugadores) {
            return ResultadoValidacion.invalido(
                    R.string.error_partido_plazas_iniciales_invalid,
                    Campo.PERSONAS_INICIALES
            );
        }

        return ResultadoValidacion.valido();
    }

    @NonNull
    public static String normalizarDeporte(@Nullable String deporte) {
        String deporteLimpio = limpiarTexto(deporte).toLowerCase(Locale.ROOT);
        if (Partido.DEPORTE_TENIS.equals(deporteLimpio)) {
            return Partido.DEPORTE_TENIS;
        }
        return Partido.DEPORTE_PADEL;
    }

    public static boolean esDeporteValido(@Nullable String deporte) {
        String deporteLimpio = limpiarTexto(deporte).toLowerCase(Locale.ROOT);
        return Partido.DEPORTE_PADEL.equals(deporteLimpio) || Partido.DEPORTE_TENIS.equals(deporteLimpio);
    }

    public static int obtenerMaxJugadores(@Nullable String deporte) {
        String deporteLimpio = normalizarDeporte(deporte);
        if (Partido.DEPORTE_TENIS.equals(deporteLimpio)) {
            return 2;
        }
        return 4;
    }

    public static int calcularPlazasOcupadasIniciales(@Nullable String deporte, int personasIniciales) {
        String deporteLimpio = normalizarDeporte(deporte);
        if (Partido.DEPORTE_TENIS.equals(deporteLimpio)) {
            return 1;
        }
        return personasIniciales;
    }

    public static boolean esPersonasInicialesValido(@Nullable String deporte, int personasIniciales) {
        String deporteLimpio = normalizarDeporte(deporte);
        if (Partido.DEPORTE_TENIS.equals(deporteLimpio)) {
            return personasIniciales == 1;
        }
        return personasIniciales == 1 || personasIniciales == 2;
    }

    public static int parsearNivel(@Nullable String nivelTexto) {
        int nivel = ValidationUtils.parseLevel(nivelTexto);
        if (nivel < ValidationUtils.MIN_LEVEL || nivel > ValidationUtils.MAX_LEVEL) {
            return -1;
        }
        return nivel;
    }

    @NonNull
    public static String limpiarTexto(@Nullable String value) {
        if (TextUtils.isEmpty(value)) {
            return "";
        }
        return value.trim();
    }

    public static boolean estaVacio(@Nullable String value) {
        return limpiarTexto(value).isEmpty();
    }
}
