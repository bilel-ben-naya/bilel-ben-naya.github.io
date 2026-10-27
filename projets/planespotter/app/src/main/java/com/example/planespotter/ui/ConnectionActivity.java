package com.example.planespotter.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.planespotter.data.OpenSkyClient;
import com.example.planespotter.data.OpenSkyHttpException;
import com.example.planespotter.R;
import com.example.planespotter.databinding.ActivityConnectionBinding;
import com.example.planespotter.util.Prefs;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConnectionActivity extends AppCompatActivity {

    private ActivityConnectionBinding b;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityConnectionBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        setupBottomNav();

        // Prefill
        b.etOpenSkyUser.setText(Prefs.getOpenSkyClientId(this));
        b.etOpenSkyPass.setText(Prefs.getOpenSkyClientSecret(this));

        b.btnSave.setOnClickListener(v -> onSave());
        b.btnTest.setOnClickListener(v -> onTest());
    }

    private void setupBottomNav() {
        BottomNavigationView nav = b.bottomNavigation;
        nav.setSelectedItemId(R.id.nav_connection);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_map) {
                startActivity(new Intent(this, MapActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_connection) {
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;
            }
            return false;
        });
    }

    private void onSave() {
        String clientId = safeTrim(b.etOpenSkyUser.getText() != null ? b.etOpenSkyUser.getText().toString() : "");
        String clientSecret = safeTrim(b.etOpenSkyPass.getText() != null ? b.etOpenSkyPass.getText().toString() : "");
        Prefs.setOpenSkyApiClient(this, clientId, clientSecret);

        Toast.makeText(this, "Connexion enregistrée", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void onTest() {
        setBusy(true);
        b.tvStatus.setVisibility(View.VISIBLE);
        b.tvStatus.setText("Test en cours...");

        final double centerLat = 48.8566;
        final double centerLon = 2.3522;
        final double deltaDeg = Prefs.getDeltaDeg(this);

        io.execute(() -> {
            try {
                // Save credentials before testing so the client uses them.
                String clientId = safeTrim(b.etOpenSkyUser.getText() != null ? b.etOpenSkyUser.getText().toString() : "");
                String clientSecret = safeTrim(b.etOpenSkyPass.getText() != null ? b.etOpenSkyPass.getText().toString() : "");
                Prefs.setOpenSkyApiClient(this, clientId, clientSecret);

                OpenSkyClient client = new OpenSkyClient(getApplicationContext());
                double minLat = centerLat - deltaDeg;
                double maxLat = centerLat + deltaDeg;
                double minLon = centerLon - deltaDeg;
                double maxLon = centerLon + deltaDeg;
                int n = client.fetchPlanes(minLat, maxLat, minLon, maxLon).size();

                runOnUiThread(() -> {
                    setBusy(false);
                    b.tvStatus.setText("Connexion OK. Avions reçus: " + n + "\n\n" +
                            "Conseil: intervalle ≥ 10s pour éviter les 429 (réglable dans Settings).");
                });
            } catch (OpenSkyHttpException e) {
                runOnUiThread(() -> {
                    setBusy(false);
                    b.tvStatus.setText(formatHttpError(e));
                });
            } catch (IOException e) {
                runOnUiThread(() -> {
                    setBusy(false);
                    b.tvStatus.setText("Erreur réseau: " + e.getMessage());
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setBusy(false);
                    b.tvStatus.setText("Erreur: " + e.getMessage());
                });
            }
        });
    }

    private String formatHttpError(OpenSkyHttpException e) {
        int code = e.getCode();
        if (code == 429) {
            return "HTTP 429 — trop de requêtes (rate limit).\n\n" +
                    "Solutions:\n" +
                    "• Mettre un intervalle ≥ 10s\n" +
                    "• Éviter d'enchaîner les tests\n" +
                    "• Configurer un API Client OpenSky (client_id / client_secret)\n";
        }
        if (code == 401 || code == 403) {
            return "Authentification refusée (" + code + ").\n\n" +
                    "OpenSky REST API utilise OAuth2. Vos identifiants du site (email/username + mot de passe) " +
                    "ne fonctionnent pas forcément pour l'API.\n\n" +
                    "À faire: sur votre compte OpenSky, créez un 'API Client' puis saisissez ici: \n" +
                    "• Client ID\n" +
                    "• Client Secret\n";
        }
        return "Erreur OpenSky (" + code + "): " + (TextUtils.isEmpty(e.getMessage()) ? "" : e.getMessage());
    }

    private void setBusy(boolean busy) {
        b.btnSave.setEnabled(!busy);
        b.btnTest.setEnabled(!busy);
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        io.shutdownNow();
    }
}
