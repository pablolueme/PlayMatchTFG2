package com.example.proyectofinaltfg2.ui;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proyectofinaltfg2.R;

public class FragmentHostActivity extends AppCompatActivity {

    public static final int CONTAINER_ID = 12345;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(R.style.Theme_ProyectoFinalApp);
        super.onCreate(savedInstanceState);

        FrameLayout container = new FrameLayout(this);
        container.setId(CONTAINER_ID);
        setContentView(container);
    }
}
