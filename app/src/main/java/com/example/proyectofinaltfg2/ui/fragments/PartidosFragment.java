package com.example.proyectofinaltfg2.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.proyectofinaltfg2.R;

public class PartidosFragment extends Fragment {

    public PartidosFragment() {
        super(R.layout.partidos_layout);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View bottomNav = view.findViewById(R.id.include_bottom_nav_partidos);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }
    }
}
