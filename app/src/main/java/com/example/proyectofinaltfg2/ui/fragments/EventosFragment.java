package com.example.proyectofinaltfg2.ui.fragments;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.proyectofinaltfg2.R;

public class EventosFragment extends Fragment {

    public EventosFragment() {
        super(R.layout.eventos_layout);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View bottomNav = view.findViewById(R.id.include_bottom_nav_eventos);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
        }
    }
}
