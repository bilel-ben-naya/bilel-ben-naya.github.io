package com.example.planespotter.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.planespotter.R;
import com.example.planespotter.data.OpenSkyClient;
import com.example.planespotter.model.FlightInfo;
import com.example.planespotter.model.Plane;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Détails d'un avion.
 *
 * Important: cette classe est volontairement alignée sur le layout existant
 * (activity_plane_details.xml) afin d'éviter les erreurs "cannot find symbol".
 */
public class PlaneDetailsActivity extends AppCompatActivity {

    private TextView tvCallsign;
    private TextView tvIcao;
    private TextView tvCountry;
    private TextView tvHeader;
    private TextView tvLastUpdate;
    private TextView tvFlightInfo;

    /** Utilisé par {@link MapActivity} pour transmettre un avion vers cet écran. */
    public static final String EXTRA_PLANE = "plane";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plane_details);

        // The toolbar in this screen is used only as a top-app-bar with an explicit back arrow.
        // We wire the navigation icon to a real "back" action (finish the Activity).
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        }

        tvCallsign = findViewById(R.id.tvCallsign);
        tvIcao = findViewById(R.id.tvIcao);
        tvCountry = findViewById(R.id.tvCountry);
        tvHeader = findViewById(R.id.tvHeader);
        tvLastUpdate = findViewById(R.id.tvLastUpdate);
        tvFlightInfo = findViewById(R.id.tvFlightInfo);

        Plane plane = (Plane) getIntent().getSerializableExtra(EXTRA_PLANE);
        if (plane == null) {
            tvHeader.setText("Aucun avion sélectionné.");
            tvFlightInfo.setText("—");
            return;
        }

        // Infos basiques
        tvCallsign.setText(safe(plane.callsign, "—"));
        tvIcao.setText(safe(plane.icao24, "—"));
        tvCountry.setText(safe(plane.originCountry, "—"));

        // Résumé technique (compact)
        String header = "Position: " + fmtLatLon(plane.latitude, plane.longitude)
                + "\nAltitude: " + fmtMeters(plane.baroAltitude)
                + "\nVitesse: " + fmtSpeed(plane.velocity)
                + "\nCap: " + fmtDegrees(plane.trueTrack)
                + "\nVario: " + fmtVerticalRate(plane.verticalRate)
                + "\nAu sol: " + (plane.onGround ? "Oui" : "Non");
        tvHeader.setText(header);

        // Dernière mise à jour
        tvLastUpdate.setText("Dernière mise à jour: " + fmtEpochSeconds(plane.lastContact));

        // Info de vol (OpenSky - endpoint flights)
        tvFlightInfo.setText("Chargement des infos de vol…");
        String icao24 = safeTrim(plane.icao24);
        if (icao24.isEmpty()) {
            tvFlightInfo.setText("Aucun identifiant ICAO24.");
            return;
        }

        // Appel réseau : on évite tout API/overload "fantôme" et on s'appuie uniquement sur
        // l'implémentation existante dans OpenSkyClient.
        final long now = System.currentTimeMillis() / 1000L;
        final long from = now - 24L * 3600L; // dernières 24h
        new Thread(() -> {
            try {
                OpenSkyClient client = new OpenSkyClient(PlaneDetailsActivity.this);
                FlightInfo flight = client.fetchLatestFlightForAircraft(icao24, from, now);
                runOnUiThread(() -> {
                    if (flight == null) {
                        tvFlightInfo.setText("Aucun vol récent trouvé.");
                    } else {
                        tvFlightInfo.setText(formatFlightInfo(flight));
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> tvFlightInfo.setText("Erreur: impossible de récupérer le vol."));
            }
        }).start();
    }

    private static String safe(String s, String fallback) {
        if (s == null) return fallback;
        String t = s.trim();
        return t.isEmpty() ? fallback : t;
    }

    @NonNull
    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private static String fmtLatLon(double lat, double lon) {
        return String.format(Locale.getDefault(), "%.5f, %.5f", lat, lon);
    }

    private static String fmtMeters(double meters) {
        // Les champs OpenSky sont en mètres.
        return String.format(Locale.getDefault(), "%.0f m", meters);
    }

    private static String fmtSpeed(double mps) {
        // OpenSky: velocity en m/s.
        double kmh = mps * 3.6;
        return String.format(Locale.getDefault(), "%.0f km/h", kmh);
    }

    private static String fmtDegrees(double degrees) {
        return String.format(Locale.getDefault(), "%.0f°", degrees);
    }

    private static String fmtVerticalRate(double ms) {
        return String.format(Locale.getDefault(), "%.1f m/s", ms);
    }

    private static String fmtEpochSeconds(long epochSeconds) {
        if (epochSeconds <= 0) return "—";
        Date d = new Date(epochSeconds * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(d);
    }

    private String formatFlightInfo(FlightInfo flight) {
        if (flight == null) return "Aucune information de vol.";

        String call = safe(flight.callsign, "—");
        String from = safe(flight.estDepartureAirport, "—");
        String to = safe(flight.estArrivalAirport, "—");
        String dep = fmtEpochSeconds(flight.firstSeen);
        String arr = fmtEpochSeconds(flight.lastSeen);

        return "Indicatif: " + call
                + "\nDépart: " + from + " (" + dep + ")"
                + "\nArrivée: " + to + " (" + arr + ")";
    }
}
