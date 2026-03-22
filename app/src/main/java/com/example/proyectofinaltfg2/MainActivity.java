package com.example.proyectofinaltfg2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinaltfg2.model.UserProfile;
import com.example.proyectofinaltfg2.ui.HomeActivity;
import com.example.proyectofinaltfg2.ui.LoginActivity;
import com.example.proyectofinaltfg2.utils.FirebaseAuthUtil;

public class MainActivity extends AppCompatActivity {

    private boolean navigationHandled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!FirebaseAuthUtil.isUserLoggedIn()) {
            navigateToLogin();
            return;
        }

        FirebaseAuthUtil.getCurrentUserProfile(this, new FirebaseAuthUtil.UserProfileCallback() {
            @Override
            public void onSuccess(@NonNull UserProfile userProfile) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                navigateToHome(userProfile);
            }

            @Override
            public void onError(@NonNull String errorMessage) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                FirebaseAuthUtil.logout();
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                navigateToLogin();
            }
        });
    }

    private void navigateToLogin() {
        if (navigationHandled) {
            return;
        }
        navigationHandled = true;
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToHome(@NonNull UserProfile userProfile) {
        if (navigationHandled) {
            return;
        }
        navigationHandled = true;
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(HomeActivity.EXTRA_ALIAS, userProfile.getAlias());
        intent.putExtra(HomeActivity.EXTRA_NOMBRE_COMPLETO, userProfile.getNombre());
        intent.putExtra(HomeActivity.EXTRA_ROLE, userProfile.getRol());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
