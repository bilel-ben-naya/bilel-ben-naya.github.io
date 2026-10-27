package com.example.planespotter.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.example.planespotter.R;
import com.example.planespotter.databinding.ActivitySettingsBinding;
import com.example.planespotter.util.Prefs;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Prefill
        binding.etIntervalSec.setText(String.valueOf(Prefs.getIntervalSec(this)));
        binding.etDeltaDeg.setText(String.valueOf(Prefs.getDeltaDeg(this)));
        binding.etMaxPlanes.setText(String.valueOf(Prefs.getMaxPlanes(this)));

        binding.btnSave.setOnClickListener(v -> save());
        binding.btnReset.setOnClickListener(v -> reset());

        setupBottomNav();
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = binding.bottomNavigation;
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.nav_settings);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_map) {
                startActivity(new Intent(this, MapActivity.class));
                return true;
            } else if (id == R.id.nav_connection) {
                startActivity(new Intent(this, ConnectionActivity.class));
                return true;
            } else if (id == R.id.nav_settings) {
                return true;
            }
            return false;
        });
    }

    private void save() {
        int interval = parseIntOr(binding.etIntervalSec.getText() != null ? binding.etIntervalSec.getText().toString() : "", Prefs.getIntervalSec(this));
        double delta = parseDoubleOr(binding.etDeltaDeg.getText() != null ? binding.etDeltaDeg.getText().toString() : "", Prefs.getDeltaDeg(this));
        int maxPlanes = parseIntOr(binding.etMaxPlanes.getText() != null ? binding.etMaxPlanes.getText().toString() : "", Prefs.getMaxPlanes(this));

        // Basic guardrails
        if (interval < 3) interval = 3;
        if (interval > 60) interval = 60;
        if (delta < 0.02) delta = 0.02;
        if (delta > 2.0) delta = 2.0;
        if (maxPlanes < 10) maxPlanes = 10;
        if (maxPlanes > 1000) maxPlanes = 1000;

        Prefs.setIntervalSec(this, interval);
        Prefs.setDeltaDeg(this, delta);
        Prefs.setMaxPlanes(this, maxPlanes);

        binding.etIntervalSec.setText(String.valueOf(interval));
        binding.etDeltaDeg.setText(String.valueOf(delta));
        binding.etMaxPlanes.setText(String.valueOf(maxPlanes));

        binding.tvSaved.setText("Enregistré. Intervalle=" + interval + "s, zone=" + delta + "°, limite=" + maxPlanes);
    }

    private void reset() {
        Prefs.setIntervalSec(this, Prefs.DEFAULT_INTERVAL_SEC);
        Prefs.setDeltaDeg(this, Prefs.DEFAULT_DELTA_DEG);
        Prefs.setMaxPlanes(this, Prefs.DEFAULT_MAX_PLANES);

        binding.etIntervalSec.setText(String.valueOf(Prefs.DEFAULT_INTERVAL_SEC));
        binding.etDeltaDeg.setText(String.valueOf(Prefs.DEFAULT_DELTA_DEG));
        binding.etMaxPlanes.setText(String.valueOf(Prefs.DEFAULT_MAX_PLANES));

        binding.tvSaved.setText("Valeurs par défaut restaurées.");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static int parseIntOr(String s, int fallback) {
        try {
            if (s == null) return fallback;
            String t = s.trim();
            if (t.isEmpty()) return fallback;
            return Integer.parseInt(t);
        } catch (Exception e) {
            return fallback;
        }
    }

    private static double parseDoubleOr(String s, double fallback) {
        try {
            if (s == null) return fallback;
            String t = s.trim();
            if (t.isEmpty()) return fallback;
            return Double.parseDouble(t);
        } catch (Exception e) {
            return fallback;
        }
    }
}
