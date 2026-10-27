package com.example.planespotter.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.planespotter.R;
import com.example.planespotter.data.OpenSkyClient;
import com.example.planespotter.data.OpenSkyHttpException;
import com.example.planespotter.model.Plane;
import com.example.planespotter.util.Prefs;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaneUpdateService extends Service {

    public static final String ACTION_PLANES = "com.example.planespotter.ACTION_PLANES";
    public static final String EXTRA_PLANES_JSON = "planes_json";
    public static final String EXTRA_ERROR = "error";
    public static final String EXTRA_CENTER_LAT = "center_lat";
    public static final String EXTRA_CENTER_LON = "center_lon";

    private static final String CHANNEL_ID = "plane_tracking";
    private static final int NOTIF_ID = 1001;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean running = false;

    private OpenSkyClient client;
    private final Gson gson = new Gson();

    // Backoff used when the API returns HTTP 429 (rate limit) or when networking fails.
    private long backoffMs = 0L;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        // Initialize the API client with an application context so it can read stored credentials.
        client = new OpenSkyClient(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Critical: call startForeground() immediately (fixes ForegroundServiceDidNotStartInTimeException)
        startForeground(NOTIF_ID, buildNotification("Fetching planes…"));

        final double centerLat = intent != null ? intent.getDoubleExtra(EXTRA_CENTER_LAT, 48.8566) : 48.8566;
        final double centerLon = intent != null ? intent.getDoubleExtra(EXTRA_CENTER_LON, 2.3522) : 2.3522;

        if (running) return START_STICKY;
        running = true;

        executor.execute(() -> {
            while (running) {
                try {
                    int interval = Prefs.getIntervalSec(getApplicationContext());
                    double delta = Prefs.getDeltaDeg(getApplicationContext());

                    double lamin = clampLat(centerLat - delta);
                    double lamax = clampLat(centerLat + delta);
                    double lomin = clampLon(centerLon - delta);
                    double lomax = clampLon(centerLon + delta);

                    List<Plane> planes = client.fetchPlanes(lamin, lomin, lamax, lomax);

                    // Reset backoff on success.
                    backoffMs = 0L;

                    Intent broadcast = new Intent(ACTION_PLANES);
                    broadcast.setPackage(getPackageName());
                    broadcast.putExtra(EXTRA_PLANES_JSON, gson.toJson(planes));
                    sendBroadcast(broadcast);

                    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                    if (nm != null) nm.notify(NOTIF_ID, buildNotification("Planes: " + planes.size()));

                    Thread.sleep(interval * 1000L);
                } catch (OpenSkyHttpException e) {
                    handleHttpError(e);
                } catch (Exception e) {
                    handleGenericError(e);
                }
            }
            stopSelf();
        });

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        running = false;
        executor.shutdownNow();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    private Notification buildNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_plane)
                .setContentTitle("PlaneSpotter")
                .setContentText(text)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Plane tracking", NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }

    private void handleHttpError(OpenSkyHttpException e) {
        final int code = e.getCode();

        // Base interval (ms) comes from user preferences.
        final long base = Math.max(10_000L, Prefs.getIntervalSec(getApplicationContext()) * 1000L);

        String msg;
        if (code == 429) {
            // Rate limit: exponential backoff capped at 2 minutes.
            backoffMs = (backoffMs <= 0L) ? (base * 2L) : Math.min(backoffMs * 2L, 120_000L);
            msg = "HTTP 429 (rate limit). Ajoute tes identifiants OpenSky dans l'onglet Connection ou augmente l'intervalle de mise à jour. Nouvelle tentative dans "
                    + (backoffMs / 1000L) + "s.";
        } else if (code == 401 || code == 403) {
            // Credentials issue: retry slower, but keep notifying user.
            backoffMs = Math.min(Math.max(base, 30_000L), 120_000L);
            msg = "Erreur d'authentification (" + code + "). Vérifie les identifiants dans l'onglet Connection. Nouvelle tentative dans "
                    + (backoffMs / 1000L) + "s.";
        } else {
            backoffMs = Math.min(Math.max(base, 15_000L), 120_000L);
            msg = "Erreur API OpenSky (" + code + "). Nouvelle tentative dans " + (backoffMs / 1000L) + "s.";
        }

        broadcastError(msg);
        safeSleep(backoffMs);
    }

    private void handleGenericError(Exception e) {
        final long base = Math.max(10_000L, Prefs.getIntervalSec(getApplicationContext()) * 1000L);
        backoffMs = Math.min(Math.max((backoffMs <= 0L ? base : backoffMs * 2L), 15_000L), 120_000L);

        String msg = "Erreur réseau/traitement: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName())
                + ". Nouvelle tentative dans " + (backoffMs / 1000L) + "s.";
        broadcastError(msg);
        safeSleep(backoffMs);
    }

    private void broadcastError(String msg) {
        Intent broadcast = new Intent(ACTION_PLANES);
        broadcast.setPackage(getPackageName());
        broadcast.putExtra(EXTRA_ERROR, msg);
        sendBroadcast(broadcast);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(NOTIF_ID, buildNotification(msg));
    }

    private void safeSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            // Service is stopping.
        }
    }

    private double clampLat(double v) { return Math.max(-85.0, Math.min(85.0, v)); }
    private double clampLon(double v) { return Math.max(-180.0, Math.min(180.0, v)); }
}
