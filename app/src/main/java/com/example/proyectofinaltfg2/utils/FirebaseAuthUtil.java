package com.example.proyectofinaltfg2.utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.proyectofinaltfg2.model.UserProfile;
import com.example.proyectofinaltfg2.R;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public final class FirebaseAuthUtil {

    private static final String USERS_COLLECTION = "users";

    public interface AuthResultCallback {
        void onSuccess();

        void onError(@NonNull String errorMessage);
    }

    public interface UserProfileCallback {
        void onSuccess(@NonNull UserProfile userProfile);

        void onError(@NonNull String errorMessage);
    }

    private FirebaseAuthUtil() {
        // Utility class
    }

    @NonNull
    private static FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    public static void login(
            @Nullable String email,
            @Nullable String password,
            @NonNull Context context,
            @NonNull AuthResultCallback callback
    ) {
        getAuth()
                .signInWithEmailAndPassword(sanitize(email), sanitize(password))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                        return;
                    }
                    callback.onError(getAuthErrorMessage(context, resolveException(task.getException())));
                });
    }

    public static void loginAndFetchProfile(
            @Nullable String email,
            @Nullable String password,
            @NonNull Context context,
            @NonNull UserProfileCallback callback
    ) {
        login(email, password, context, new AuthResultCallback() {
            @Override
            public void onSuccess() {
                getCurrentUserProfile(context, callback);
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public static void registerWithProfile(
            @NonNull UserProfile userProfile,
            @Nullable String password,
            @NonNull Context context,
            @NonNull AuthResultCallback callback
    ) {
        getAuth()
                .createUserWithEmailAndPassword(
                        sanitize(userProfile.getEmail()),
                        sanitize(password)
                )
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = getCurrentUser();
                        if (firebaseUser == null) {
                            callback.onError(context.getString(R.string.msg_auth_generic_error));
                            return;
                        }

                        userProfile.setUid(firebaseUser.getUid());
                        saveUserProfile(userProfile, context, new AuthResultCallback() {
                            @Override
                            public void onSuccess() {
                                callback.onSuccess();
                            }

                            @Override
                            public void onError(@NonNull String errorMessage) {
                                // Evita dejar cuenta huerfana en Auth si falla Firestore.
                                firebaseUser.delete();
                                callback.onError(errorMessage);
                            }
                        });
                        return;
                    }
                    callback.onError(getAuthErrorMessage(context, resolveException(task.getException())));
                });
    }

    public static void saveUserProfile(
            @NonNull UserProfile userProfile,
            @NonNull Context context,
            @NonNull AuthResultCallback callback
    ) {
        getFirestore()
                .collection(USERS_COLLECTION)
                .document(userProfile.getUid())
                .set(userProfile)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(exception ->
                        callback.onError(getAuthErrorMessage(context, exception))
                );
    }

    public static void getCurrentUserProfile(
            @NonNull Context context,
            @NonNull UserProfileCallback callback
    ) {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null) {
            callback.onError(context.getString(R.string.msg_auth_invalid_credentials));
            return;
        }
        getUserProfile(currentUser.getUid(), context, callback);
    }

    public static void getUserProfile(
            @NonNull String uid,
            @NonNull Context context,
            @NonNull UserProfileCallback callback
    ) {
        getFirestore()
                .collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot ->
                        handleUserProfileDocument(documentSnapshot, context, callback)
                )
                .addOnFailureListener(exception ->
                        callback.onError(getAuthErrorMessage(context, exception))
                );
    }

    public static void sendPasswordResetEmail(
            @Nullable String email,
            @NonNull Context context,
            @NonNull AuthResultCallback callback
    ) {
        getAuth()
                .sendPasswordResetEmail(sanitize(email))
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess();
                        return;
                    }
                    callback.onError(getAuthErrorMessage(context, resolveException(task.getException())));
                });
    }

    public static void logout() {
        getAuth().signOut();
    }

    public static boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }

    @Nullable
    public static FirebaseUser getCurrentUser() {
        return getAuth().getCurrentUser();
    }

    @NonNull
    public static String getCurrentUserEmail() {
        FirebaseUser currentUser = getCurrentUser();
        if (currentUser == null || currentUser.getEmail() == null) {
            return "";
        }
        return currentUser.getEmail();
    }

    @NonNull
    public static String getAuthErrorMessage(@NonNull Context context, @NonNull Exception exception) {
        if (exception instanceof FirebaseAuthWeakPasswordException) {
            return context.getString(R.string.error_password_min_length);
        }

        if (exception instanceof FirebaseAuthUserCollisionException) {
            return context.getString(R.string.msg_auth_email_already_registered);
        }

        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            FirebaseAuthInvalidCredentialsException credentialsException =
                    (FirebaseAuthInvalidCredentialsException) exception;
            String errorCode = credentialsException.getErrorCode();

            if ("ERROR_INVALID_EMAIL".equals(errorCode)) {
                return context.getString(R.string.error_invalid_email_format);
            }
            if ("ERROR_WRONG_PASSWORD".equals(errorCode)
                    || "ERROR_INVALID_CREDENTIAL".equals(errorCode)) {
                return context.getString(R.string.msg_auth_invalid_credentials);
            }
            return context.getString(R.string.msg_auth_invalid_credentials);
        }

        if (exception instanceof FirebaseAuthInvalidUserException) {
            FirebaseAuthInvalidUserException invalidUserException =
                    (FirebaseAuthInvalidUserException) exception;
            String errorCode = invalidUserException.getErrorCode();

            if ("ERROR_USER_NOT_FOUND".equals(errorCode)) {
                return context.getString(R.string.msg_auth_user_not_found);
            }
            if ("ERROR_USER_DISABLED".equals(errorCode)) {
                return context.getString(R.string.msg_auth_user_disabled);
            }
            return context.getString(R.string.msg_auth_invalid_credentials);
        }

        if (exception instanceof FirebaseNetworkException) {
            return context.getString(R.string.msg_auth_network_error);
        }

        if (exception instanceof FirebaseTooManyRequestsException) {
            return context.getString(R.string.msg_auth_too_many_requests);
        }

        return context.getString(R.string.msg_auth_generic_error);
    }

    @NonNull
    private static FirebaseFirestore getFirestore() {
        return FirebaseFirestore.getInstance();
    }

    private static void handleUserProfileDocument(
            @NonNull DocumentSnapshot documentSnapshot,
            @NonNull Context context,
            @NonNull UserProfileCallback callback
    ) {
        if (!documentSnapshot.exists()) {
            callback.onError(context.getString(R.string.msg_profile_not_found));
            return;
        }

        UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
        if (userProfile == null) {
            callback.onError(context.getString(R.string.msg_profile_not_found));
            return;
        }

        if (TextUtils.isEmpty(userProfile.getUid())) {
            userProfile.setUid(documentSnapshot.getId());
        }

        callback.onSuccess(userProfile);
    }

    @NonNull
    private static String sanitize(@Nullable String value) {
        if (TextUtils.isEmpty(value)) {
            return "";
        }
        return value.trim();
    }

    @NonNull
    private static Exception resolveException(@Nullable Exception exception) {
        return exception != null
                ? exception
                : new IllegalStateException("FirebaseAuth devolvio una excepcion nula");
    }
}
