package com.example.proyectofinaltfg2.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.proyectofinaltfg2.R;
import com.example.proyectofinaltfg2.ui.fragments.EventosFragment;
import com.example.proyectofinaltfg2.ui.fragments.InicioFragment;
import com.example.proyectofinaltfg2.ui.fragments.PartidosFragment;
import com.example.proyectofinaltfg2.ui.fragments.PerfilFragment;
import com.example.proyectofinaltfg2.utils.FirebaseAuthUtil;

public class HomeActivity extends AppCompatActivity {

    public static final String EXTRA_ALIAS = "extra_alias";
    public static final String EXTRA_NOMBRE_COMPLETO = "extra_nombre_completo";
    public static final String EXTRA_ROLE = "extra_role";

    private static final String STATE_SELECTED_TAB = "state_selected_tab";

    private enum MainTab {
        INICIO,
        PARTIDOS,
        EVENTOS,
        PERFIL
    }

    private LinearLayout itemNavInicio;
    private LinearLayout itemNavPartidos;
    private LinearLayout itemNavEventos;
    private LinearLayout itemNavPerfil;
    private ImageView iconNavInicio;
    private ImageView iconNavPartidos;
    private ImageView iconNavEventos;
    private ImageView iconNavPerfil;
    private TextView txtNavInicio;
    private TextView txtNavPartidos;
    private TextView txtNavEventos;
    private TextView txtNavPerfil;

    private MainTab selectedTab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!FirebaseAuthUtil.isUserLoggedIn()) {
            navigateToLogin();
            finish();
            return;
        }

        setContentView(R.layout.home_activity_layout);
        bindViews();
        configureListeners();

        if (savedInstanceState == null) {
            switchTab(MainTab.INICIO);
        } else {
            selectedTab = parseTab(savedInstanceState.getString(STATE_SELECTED_TAB));
            Fragment restoredTabFragment = getSupportFragmentManager()
                    .findFragmentByTag(selectedTab.name());
            if (restoredTabFragment == null) {
                switchTab(selectedTab);
                return;
            }
            updateBottomNavState(selectedTab);
        }
    }

    @Override
    public void onBackPressed() {
        if (selectedTab != MainTab.INICIO) {
            switchTab(MainTab.INICIO);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        MainTab currentTab = selectedTab == null ? MainTab.INICIO : selectedTab;
        outState.putString(STATE_SELECTED_TAB, currentTab.name());
    }

    private void bindViews() {
        View bottomNav = findViewById(R.id.include_bottom_nav_main);
        itemNavInicio = bottomNav.findViewById(R.id.item_nav_inicio);
        itemNavPartidos = bottomNav.findViewById(R.id.item_nav_partidos);
        itemNavEventos = bottomNav.findViewById(R.id.item_nav_eventos);
        itemNavPerfil = bottomNav.findViewById(R.id.item_nav_perfil);
        iconNavInicio = bottomNav.findViewById(R.id.icon_nav_inicio);
        iconNavPartidos = bottomNav.findViewById(R.id.icon_nav_partidos);
        iconNavEventos = bottomNav.findViewById(R.id.icon_nav_eventos);
        iconNavPerfil = bottomNav.findViewById(R.id.icon_nav_perfil);
        txtNavInicio = bottomNav.findViewById(R.id.txt_nav_inicio);
        txtNavPartidos = bottomNav.findViewById(R.id.txt_nav_partidos);
        txtNavEventos = bottomNav.findViewById(R.id.txt_nav_eventos);
        txtNavPerfil = bottomNav.findViewById(R.id.txt_nav_perfil);
    }

    private void configureListeners() {
        itemNavInicio.setOnClickListener(v -> switchTab(MainTab.INICIO));
        itemNavPartidos.setOnClickListener(v -> switchTab(MainTab.PARTIDOS));
        itemNavEventos.setOnClickListener(v -> switchTab(MainTab.EVENTOS));
        itemNavPerfil.setOnClickListener(v -> switchTab(MainTab.PERFIL));
    }

    private void switchTab(@NonNull MainTab targetTab) {
        if (targetTab == selectedTab) {
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (selectedTab != null) {
            Fragment currentFragment = fragmentManager.findFragmentByTag(selectedTab.name());
            if (currentFragment != null) {
                transaction.hide(currentFragment);
            }
        }

        Fragment targetFragment = fragmentManager.findFragmentByTag(targetTab.name());
        if (targetFragment == null) {
            targetFragment = createFragmentForTab(targetTab);
            transaction.add(R.id.container_main_tabs, targetFragment, targetTab.name());
        } else {
            transaction.show(targetFragment);
        }

        transaction.commit();
        selectedTab = targetTab;
        updateBottomNavState(targetTab);
    }

    @NonNull
    private Fragment createFragmentForTab(@NonNull MainTab tab) {
        if (tab == MainTab.PARTIDOS) {
            return new PartidosFragment();
        }
        if (tab == MainTab.EVENTOS) {
            return new EventosFragment();
        }
        if (tab == MainTab.PERFIL) {
            return new PerfilFragment();
        }
        return new InicioFragment();
    }

    private void updateBottomNavState(@Nullable MainTab activeTab) {
        MainTab tab = activeTab == null ? MainTab.INICIO : activeTab;
        setNavItemState(iconNavInicio, txtNavInicio, tab == MainTab.INICIO);
        setNavItemState(iconNavPartidos, txtNavPartidos, tab == MainTab.PARTIDOS);
        setNavItemState(iconNavEventos, txtNavEventos, tab == MainTab.EVENTOS);
        setNavItemState(iconNavPerfil, txtNavPerfil, tab == MainTab.PERFIL);
    }

    private void setNavItemState(
            @NonNull ImageView iconView,
            @NonNull TextView textView,
            boolean isActive
    ) {
        int colorRes = isActive ? R.color.primary_brand : R.color.nav_icon_inactive;
        int colorValue = ContextCompat.getColor(this, colorRes);
        iconView.setColorFilter(colorValue);
        textView.setTextColor(colorValue);
    }

    @NonNull
    private MainTab parseTab(@Nullable String tabName) {
        if (tabName == null) {
            return MainTab.INICIO;
        }
        try {
            return MainTab.valueOf(tabName);
        } catch (IllegalArgumentException ignored) {
            return MainTab.INICIO;
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
