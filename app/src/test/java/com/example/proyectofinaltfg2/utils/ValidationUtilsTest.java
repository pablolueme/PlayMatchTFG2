package com.example.proyectofinaltfg2.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.proyectofinaltfg2.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ValidationUtilsTest {

    @Test
    public void isFieldEmpty_cubreNullVacioEspaciosYTexto() {
        assertTrue(ValidationUtils.isFieldEmpty(null));
        assertTrue(ValidationUtils.isFieldEmpty(""));
        assertTrue(ValidationUtils.isFieldEmpty("   "));
        assertFalse(ValidationUtils.isFieldEmpty("abc"));
    }

    @Test
    public void isEmailValid_cubreNullInvalidoYValido() {
        assertFalse(ValidationUtils.isEmailValid(null));
        assertFalse(ValidationUtils.isEmailValid("correo-invalido"));
        assertTrue(ValidationUtils.isEmailValid("  ana@test.com  "));
    }

    @Test
    public void isPasswordValid_cubreLimites() {
        assertFalse(ValidationUtils.isPasswordValid(null));
        assertFalse(ValidationUtils.isPasswordValid("12345"));
        assertTrue(ValidationUtils.isPasswordValid("123456"));
    }

    @Test
    public void doPasswordsMatch_cubreNullYEspacios() {
        assertFalse(ValidationUtils.doPasswordsMatch(null, "123456"));
        assertFalse(ValidationUtils.doPasswordsMatch("123456", null));
        assertTrue(ValidationUtils.doPasswordsMatch(" 123456 ", "123456"));
        assertFalse(ValidationUtils.doPasswordsMatch("123456", "654321"));
    }

    @Test
    public void clubCode_parseLevelYRangos() {
        assertFalse(ValidationUtils.isValidClubCode(null));
        assertFalse(ValidationUtils.isValidClubCode("otro"));
        assertTrue(ValidationUtils.isValidClubCode(ValidationUtils.CLUB_ACCESS_CODE));

        assertEquals(-1, ValidationUtils.parseLevel(null));
        assertEquals(-1, ValidationUtils.parseLevel(" "));
        assertEquals(-1, ValidationUtils.parseLevel("abc"));
        assertEquals(3, ValidationUtils.parseLevel(" 3 "));
        assertFalse(ValidationUtils.isValidLevelSelection("0"));
        assertTrue(ValidationUtils.isValidLevelSelection("1"));
        assertTrue(ValidationUtils.isValidLevelSelection("5"));
        assertFalse(ValidationUtils.isValidLevelSelection("6"));
    }

    @Test
    public void validateLogin_cubreRamasPrincipales() {
        ValidationUtils.ValidationResult emailVacio = ValidationUtils.validateLogin(" ", "123456");
        assertFalse(emailVacio.isValid());
        assertEquals(R.string.error_email_required, emailVacio.getMessageResId());
        assertEquals(ValidationUtils.Field.EMAIL, emailVacio.getField());

        ValidationUtils.ValidationResult emailInvalido = ValidationUtils.validateLogin("mal", "123456");
        assertFalse(emailInvalido.isValid());
        assertEquals(R.string.error_invalid_email_format, emailInvalido.getMessageResId());

        ValidationUtils.ValidationResult passwordVacio = ValidationUtils.validateLogin("a@test.com", " ");
        assertFalse(passwordVacio.isValid());
        assertEquals(R.string.error_password_required, passwordVacio.getMessageResId());
        assertEquals(ValidationUtils.Field.PASSWORD, passwordVacio.getField());

        ValidationUtils.ValidationResult valido = ValidationUtils.validateLogin("a@test.com", "123456");
        assertTrue(valido.isValid());
        assertEquals(0, valido.getMessageResId());
        assertNull(valido.getField());
    }

    @Test
    public void validateRecoveryEmail_cubreRamas() {
        ValidationUtils.ValidationResult vacio = ValidationUtils.validateRecoveryEmail(" ");
        assertFalse(vacio.isValid());
        assertEquals(R.string.error_email_required, vacio.getMessageResId());

        ValidationUtils.ValidationResult invalido = ValidationUtils.validateRecoveryEmail("xx");
        assertFalse(invalido.isValid());
        assertEquals(R.string.error_invalid_email_format, invalido.getMessageResId());

        ValidationUtils.ValidationResult valido = ValidationUtils.validateRecoveryEmail("ok@test.com");
        assertTrue(valido.isValid());
    }

    @Test
    public void validateRegister_cubreErroresDeCamposBasicos() {
        assertErrorRegistro(
                ValidationUtils.validateRegister(
                        " ", "alias", "a@test.com", "123456", "123456",
                        ValidationUtils.ROLE_USER, "", "3", "3"
                ),
                R.string.error_name_required,
                ValidationUtils.Field.NAME
        );
        assertErrorRegistro(
                ValidationUtils.validateRegister(
                        "Nombre", " ", "a@test.com", "123456", "123456",
                        ValidationUtils.ROLE_USER, "", "3", "3"
                ),
                R.string.error_alias_required,
                ValidationUtils.Field.ALIAS
        );
        assertErrorRegistro(
                ValidationUtils.validateRegister(
                        "Nombre", "alias", " ", "123456", "123456",
                        ValidationUtils.ROLE_USER, "", "3", "3"
                ),
                R.string.error_email_required,
                ValidationUtils.Field.EMAIL
        );
        assertErrorRegistro(
                ValidationUtils.validateRegister(
                        "Nombre", "alias", "correo", "123456", "123456",
                        ValidationUtils.ROLE_USER, "", "3", "3"
                ),
                R.string.error_invalid_email_format,
                ValidationUtils.Field.EMAIL
        );
        assertErrorRegistro(
                ValidationUtils.validateRegister(
                        "Nombre", "alias", "a@test.com", " ", "123456",
                        ValidationUtils.ROLE_USER, "", "3", "3"
                ),
                R.string.error_password_required,
                ValidationUtils.Field.PASSWORD
        );
        assertErrorRegistro(
                ValidationUtils.validateRegister(
                        "Nombre", "alias", "a@test.com", "12345", "12345",
                        ValidationUtils.ROLE_USER, "", "3", "3"
                ),
                R.string.error_password_min_length,
                ValidationUtils.Field.PASSWORD
        );
        assertErrorRegistro(
                ValidationUtils.validateRegister(
                        "Nombre", "alias", "a@test.com", "123456", " ",
                        ValidationUtils.ROLE_USER, "", "3", "3"
                ),
                R.string.error_confirm_password_required,
                ValidationUtils.Field.CONFIRM_PASSWORD
        );
        assertErrorRegistro(
                ValidationUtils.validateRegister(
                        "Nombre", "alias", "a@test.com", "123456", "654321",
                        ValidationUtils.ROLE_USER, "", "3", "3"
                ),
                R.string.error_passwords_do_not_match,
                ValidationUtils.Field.CONFIRM_PASSWORD
        );
    }

    @Test
    public void validateRegister_cubreClubYNivelesYCasoValido() {
        assertErrorRegistro(
                ValidationUtils.validateRegister(
                        "Nombre", "alias", "a@test.com", "123456", "123456",
                        ValidationUtils.ROLE_CLUB, " ", "3", "3"
                ),
                R.string.error_club_code_required,
                ValidationUtils.Field.CLUB_CODE
        );
        assertErrorRegistro(
                ValidationUtils.validateRegister(
                        "Nombre", "alias", "a@test.com", "123456", "123456",
                        ValidationUtils.ROLE_CLUB, "invalido", "3", "3"
                ),
                R.string.error_club_code_invalid,
                ValidationUtils.Field.CLUB_CODE
        );
        assertErrorRegistro(
                ValidationUtils.validateRegister(
                        "Nombre", "alias", "a@test.com", "123456", "123456",
                        ValidationUtils.ROLE_USER, "", "0", "3"
                ),
                R.string.error_nivel_padel_required,
                ValidationUtils.Field.NIVEL_PADEL
        );
        assertErrorRegistro(
                ValidationUtils.validateRegister(
                        "Nombre", "alias", "a@test.com", "123456", "123456",
                        ValidationUtils.ROLE_USER, "", "3", "9"
                ),
                R.string.error_nivel_tenis_required,
                ValidationUtils.Field.NIVEL_TENIS
        );

        ValidationUtils.ValidationResult valido = ValidationUtils.validateRegister(
                "Nombre",
                "alias",
                "a@test.com",
                "123456",
                "123456",
                ValidationUtils.ROLE_CLUB,
                ValidationUtils.CLUB_ACCESS_CODE,
                "3",
                "4"
        );
        assertTrue(valido.isValid());
    }

    private void assertErrorRegistro(
            ValidationUtils.ValidationResult result,
            int mensajeRes,
            ValidationUtils.Field field
    ) {
        assertFalse(result.isValid());
        assertEquals(mensajeRes, result.getMessageResId());
        assertEquals(field, result.getField());
    }
}
