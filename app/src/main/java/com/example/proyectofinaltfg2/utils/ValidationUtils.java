package com.example.proyectofinaltfg2.utils;

import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.example.proyectofinaltfg2.R;

public final class ValidationUtils {

    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 5;
    public static final String ROLE_USER = "USER";
    public static final String ROLE_CLUB = "CLUB";
    public static final String CLUB_ACCESS_CODE = "PlayMatch2026PadelTenisClub";

    public enum Field {
        NAME,
        ALIAS,
        EMAIL,
        PASSWORD,
        CONFIRM_PASSWORD,
        CLUB_CODE,
        NIVEL_PADEL,
        NIVEL_TENIS
    }

    public static final class ValidationResult {
        private final boolean valid;
        @StringRes
        private final int messageResId;
        @Nullable
        private final Field field;

        private ValidationResult(boolean valid, @StringRes int messageResId, @Nullable Field field) {
            this.valid = valid;
            this.messageResId = messageResId;
            this.field = field;
        }

        @NonNull
        public static ValidationResult valid() {
            return new ValidationResult(true, 0, null);
        }

        @NonNull
        public static ValidationResult invalid(@StringRes int messageResId, @NonNull Field field) {
            return new ValidationResult(false, messageResId, field);
        }

        public boolean isValid() {
            return valid;
        }

        @StringRes
        public int getMessageResId() {
            return messageResId;
        }

        @Nullable
        public Field getField() {
            return field;
        }
    }

    private ValidationUtils() {
        // Utility class
    }

    @NonNull
    public static ValidationResult validateLogin(@Nullable String email, @Nullable String password) {
        if (isFieldEmpty(email)) {
            return ValidationResult.invalid(R.string.error_email_required, Field.EMAIL);
        }
        if (!isEmailValid(email)) {
            return ValidationResult.invalid(R.string.error_invalid_email_format, Field.EMAIL);
        }
        if (isFieldEmpty(password)) {
            return ValidationResult.invalid(R.string.error_password_required, Field.PASSWORD);
        }
        return ValidationResult.valid();
    }

    @NonNull
    public static ValidationResult validateRegister(
            @Nullable String fullName,
            @Nullable String alias,
            @Nullable String email,
            @Nullable String password,
            @Nullable String confirmPassword,
            @Nullable String role,
            @Nullable String clubCode,
            @Nullable String nivelPadel,
            @Nullable String nivelTenis
    ) {
        if (isFieldEmpty(fullName)) {
            return ValidationResult.invalid(R.string.error_name_required, Field.NAME);
        }
        if (isFieldEmpty(alias)) {
            return ValidationResult.invalid(R.string.error_alias_required, Field.ALIAS);
        }
        if (isFieldEmpty(email)) {
            return ValidationResult.invalid(R.string.error_email_required, Field.EMAIL);
        }
        if (!isEmailValid(email)) {
            return ValidationResult.invalid(R.string.error_invalid_email_format, Field.EMAIL);
        }
        if (isFieldEmpty(password)) {
            return ValidationResult.invalid(R.string.error_password_required, Field.PASSWORD);
        }
        if (!isPasswordValid(password)) {
            return ValidationResult.invalid(R.string.error_password_min_length, Field.PASSWORD);
        }
        if (isFieldEmpty(confirmPassword)) {
            return ValidationResult.invalid(R.string.error_confirm_password_required, Field.CONFIRM_PASSWORD);
        }
        if (!doPasswordsMatch(password, confirmPassword)) {
            return ValidationResult.invalid(R.string.error_passwords_do_not_match, Field.CONFIRM_PASSWORD);
        }

        if (ROLE_CLUB.equals(role)) {
            if (isFieldEmpty(clubCode)) {
                return ValidationResult.invalid(R.string.error_club_code_required, Field.CLUB_CODE);
            }
            if (!isValidClubCode(clubCode)) {
                return ValidationResult.invalid(R.string.error_club_code_invalid, Field.CLUB_CODE);
            }
        }

        if (!isValidLevelSelection(nivelPadel)) {
            return ValidationResult.invalid(R.string.error_nivel_padel_required, Field.NIVEL_PADEL);
        }
        if (!isValidLevelSelection(nivelTenis)) {
            return ValidationResult.invalid(R.string.error_nivel_tenis_required, Field.NIVEL_TENIS);
        }

        return ValidationResult.valid();
    }

    @NonNull
    public static ValidationResult validateRecoveryEmail(@Nullable String email) {
        if (isFieldEmpty(email)) {
            return ValidationResult.invalid(R.string.error_email_required, Field.EMAIL);
        }
        if (!isEmailValid(email)) {
            return ValidationResult.invalid(R.string.error_invalid_email_format, Field.EMAIL);
        }
        return ValidationResult.valid();
    }

    public static boolean isFieldEmpty(@Nullable String value) {
        return TextUtils.isEmpty(value) || TextUtils.isEmpty(value.trim());
    }

    public static boolean isEmailValid(@Nullable String email) {
        if (isFieldEmpty(email)) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    public static boolean isPasswordValid(@Nullable String password) {
        if (isFieldEmpty(password)) {
            return false;
        }
        return password.trim().length() >= MIN_PASSWORD_LENGTH;
    }

    public static boolean doPasswordsMatch(@Nullable String password, @Nullable String confirmPassword) {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.trim().equals(confirmPassword.trim());
    }

    public static boolean isValidClubCode(@Nullable String clubCode) {
        if (isFieldEmpty(clubCode)) {
            return false;
        }
        return CLUB_ACCESS_CODE.equals(clubCode.trim());
    }

    public static boolean isValidLevelSelection(@Nullable String value) {
        int level = parseLevel(value);
        return level >= MIN_LEVEL && level <= MAX_LEVEL;
    }

    public static int parseLevel(@Nullable String value) {
        if (isFieldEmpty(value)) {
            return -1;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }
}
