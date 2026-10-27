package com.example.planespotter.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.planespotter.R;
import com.example.planespotter.databinding.ActivityMapBinding;
import com.example.planespotter.model.Plane;
import com.example.planespotter.service.PlaneUpdateService;
import com.example.planespotter.util.Prefs;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.LruCache;
import androidx.annotation.ColorInt;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.DrawableCompat;

public class MapActivity extends AppCompatActivity {

    private ActivityMapBinding binding;

    private final Gson gson = new Gson();
    private final Type planeListType = new TypeToken<List<Plane>>() {}.getType();

    /** Keep markers & tracks stable across refreshes (prevents flicker, enables trails). */
    private final Map<String, Marker> markersByIcao24 = new HashMap<>();
    private final Map<String, Polyline> tracksByIcao24 = new HashMap<>();
    private final Map<String, ArrayList<GeoPoint>> trackPointsByIcao24 = new HashMap<>();

    /** Cached marker icon to avoid re-inflating drawables on each refresh. */
    private final Map<Integer, Drawable> planeIconCache = new HashMap<>();

    private final BroadcastReceiver planesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String err = intent.getStringExtra(PlaneUpdateService.EXTRA_ERROR);
            if (err != null) {
                binding.tvStatus.setText("Error: " + err);
                return;
            }

            String json = intent.getStringExtra(PlaneUpdateService.EXTRA_PLANES_JSON);
            if (json == null) return;

            List<Plane> planes = gson.fromJson(json, planeListType);
            if (planes == null) planes = new ArrayList<>();

            int max = Prefs.getMaxPlanes(getApplicationContext());
            updateMarkers(planes, max);
            binding.tvStatus.setText("Running — planes: " + Math.min(planes.size(), max));
        }
    };

    private final ActivityResultLauncher<String> notifPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startTracking();
                else binding.tvStatus.setText("Notification permission denied (service may be blocked).");
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Configuration.getInstance().setUserAgentValue(getPackageName());
        binding.map.setTileSource(TileSourceFactory.MAPNIK);
        binding.map.setMultiTouchControls(true);

        binding.map.getController().setZoom(12.0);
        binding.map.getController().setCenter(new GeoPoint(48.8566, 2.3522));

        setupBottomNav(binding.bottomNav);

        binding.btnStart.setOnClickListener(v -> ensureNotifPermissionThenStart());
        binding.btnStop.setOnClickListener(v -> stopTracking());
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(PlaneUpdateService.ACTION_PLANES);
if (Build.VERSION.SDK_INT >= 33) {
    registerReceiver(planesReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
} else {
    registerReceiver(planesReceiver, filter);
}
        binding.map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(planesReceiver);
        binding.map.onPause();
    }

    private void setupBottomNav(BottomNavigationView nav) {
        nav.setSelectedItemId(R.id.nav_map);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_map) return true;

            if (id == R.id.nav_connection) {
                startActivity(new Intent(this, ConnectionActivity.class));
                return true;
            }
            if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }

    private void ensureNotifPermissionThenStart() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                return;
            }
        }
        startTracking();
    }

    private void startTracking() {
        GeoPoint c = (GeoPoint) binding.map.getMapCenter();
        Intent svc = new Intent(this, PlaneUpdateService.class);
        svc.putExtra(PlaneUpdateService.EXTRA_CENTER_LAT, c.getLatitude());
        svc.putExtra(PlaneUpdateService.EXTRA_CENTER_LON, c.getLongitude());
        ContextCompat.startForegroundService(this, svc);
        binding.tvStatus.setText(getString(R.string.status_running));
    }

    private void stopTracking() {
        stopService(new Intent(this, PlaneUpdateService.class));
        clearMarkers();
        binding.tvStatus.setText(getString(R.string.status_idle));
    }

    /**
     * Remove all dynamically added plane markers and flight path polylines.
     * Keeps the base map overlays intact.
     */
    private void clearMarkers() {
        // Remove markers
        for (Marker m : markersByIcao24.values()) {
            if (m != null) binding.map.getOverlays().remove(m);
        }
        markersByIcao24.clear();

        // Remove polylines
        for (Polyline pl : tracksByIcao24.values()) {
            if (pl != null) binding.map.getOverlays().remove(pl);
        }
        tracksByIcao24.clear();
        trackPointsByIcao24.clear();

        binding.map.invalidate();
    }

    private void updateMarkers(List<Plane> planes, int maxPlanes) {
        final int count = Math.min(planes.size(), Math.max(1, maxPlanes));

        // Track which planes are currently displayed so we can remove stale markers/tracks.
        final Set<String> seen = new HashSet<>();

        for (int i = 0; i < count; i++) {
            final Plane p = planes.get(i);
            if (p == null) continue;
            if (p.icao24 == null || p.icao24.trim().isEmpty()) continue;

            final String key = p.icao24.trim();
            seen.add(key);

            final GeoPoint pos = new GeoPoint(p.latitude, p.longitude);

            // 1) Marker (airplane icon + rotation)
            Marker m = markersByIcao24.get(key);
            if (m == null) {
                m = new Marker(binding.map);
                // Anchor au centre pour que la rotation corresponde bien au cap (nez de l'avion dans la bonne direction)
                m.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                m.setOnMarkerClickListener((marker, mapView) -> {
                    Object obj = marker.getRelatedObject();
                    if (obj instanceof Plane) {
                        Intent it = new Intent(MapActivity.this, PlaneDetailsActivity.class);
                        it.putExtra(PlaneDetailsActivity.EXTRA_PLANE, (Plane) obj);
                        startActivity(it);
                    }
                    return true; // consume
                });
                binding.map.getOverlays().add(m);
                markersByIcao24.put(key, m);
            }
            // Mise à jour dynamique (position, cap, couleur)
            final int color = planeColor(p);
            m.setIcon(getPlaneIcon(color));
            m.setRelatedObject(p);
            m.setPosition(pos);
            // OpenSky "trueTrack" is degrees clockwise from North (0°=North, 90°=East).
            // osmdroid's Marker rotation is sometimes treated as counter-clockwise depending
            // on the rendering pipeline. To align the plane nose with the real direction,
            // we convert clockwise bearing to the equivalent counter-clockwise rotation.
            m.setRotation(toMarkerRotation(p.trueTrack));

            final String cs = (p.callsign != null && !p.callsign.trim().isEmpty()) ? p.callsign.trim() : key;
            m.setTitle(cs);
            // Keep short summary in case user opens default info window in future
            m.setSubDescription(p.originCountry != null ? p.originCountry : "");

            // 2) Track polyline (simple history of positions while the app is open)
            ArrayList<GeoPoint> pts = trackPointsByIcao24.get(key);
            if (pts == null) {
                pts = new ArrayList<>();
                trackPointsByIcao24.put(key, pts);
            }

            // Avoid duplicates (and avoid over-plotting if the API returns unchanged coords)
            if (pts.isEmpty() || pts.get(pts.size() - 1).distanceToAsDouble(pos) > 20.0) {
                pts.add(pos);
                // Keep only recent N points to avoid unbounded memory growth
                while (pts.size() > 25) {
                    pts.remove(0);
                }
            }

            // Keep the trace color consistent with the plane marker tint.
            final int planeTint = color;
            final int traceColor = Color.argb(160, Color.red(planeTint), Color.green(planeTint), Color.blue(planeTint));

            Polyline line = tracksByIcao24.get(key);
            if (line == null) {
                line = new Polyline();
                line.getOutlinePaint().setStrokeWidth(4f);
                line.getOutlinePaint().setColor(traceColor);
                line.setPoints(pts);
                binding.map.getOverlays().add(line);
                tracksByIcao24.put(key, line);
            } else {
                line.getOutlinePaint().setColor(traceColor);
                line.setPoints(pts);
            }
        }

        // Remove stale overlays (planes that are no longer in the refreshed list)
        clearStale(seen);

        binding.map.invalidate();
    }

    private void clearStale(Set<String> seen) {
        // Remove markers
        for (String key : new HashSet<>(markersByIcao24.keySet())) {
            if (seen.contains(key)) continue;
            Marker m = markersByIcao24.remove(key);
            if (m != null) {
                binding.map.getOverlays().remove(m);
            }
        }

        // Remove tracks
        for (String key : new HashSet<>(tracksByIcao24.keySet())) {
            if (seen.contains(key)) continue;
            Polyline l = tracksByIcao24.remove(key);
            if (l != null) {
                binding.map.getOverlays().remove(l);
            }
            trackPointsByIcao24.remove(key);
        }
    }
@ColorInt
    private int planeColor(Plane p) {
        // Prefer geometric altitude (if present), otherwise barometric.
        final Double alt = (p.geoAltitude != null) ? p.geoAltitude : p.baroAltitude;

        if (alt != null && !alt.isNaN() && !alt.isInfinite()) {
            return altitudeToColor(alt, p.icao24);
        }
        // Fallback: stable “random” color per aircraft.
        return stableColorFromKey(p.icao24);
    }

    @ColorInt
    private int altitudeToColor(double altitudeMeters, String key) {
        // Palette *discrète* (très contrastée) pour que les altitudes soient clairement
        // distinguables sur une carte.
        // 0–2km: bleu, 2–4km: indigo, 4–6km: violet, 6–8km: fuchsia, 8–10km: rose,
        // 10–12km+: rouge.
        // (La légende utilise le même ordre de couleurs.)
        double a = Math.max(0.0, altitudeMeters);

        if (a < 2000.0)  return Color.parseColor("#1D4ED8"); // Blue (600)
        if (a < 4000.0)  return Color.parseColor("#4F46E5"); // Indigo (600)
        if (a < 6000.0)  return Color.parseColor("#7C3AED"); // Violet (600)
        if (a < 8000.0)  return Color.parseColor("#C026D3"); // Fuchsia (600)
        if (a < 10000.0) return Color.parseColor("#E11D48"); // Rose (600)
        return Color.parseColor("#B91C1C");                  // Red (700)
    }

    @ColorInt
    private static int lerpColor(@ColorInt int c1, @ColorInt int c2, float t) {
        t = Math.max(0f, Math.min(1f, t));
        final int a = (int) (Color.alpha(c1) + (Color.alpha(c2) - Color.alpha(c1)) * t);
        final int r = (int) (Color.red(c1) + (Color.red(c2) - Color.red(c1)) * t);
        final int g = (int) (Color.green(c1) + (Color.green(c2) - Color.green(c1)) * t);
        final int b = (int) (Color.blue(c1) + (Color.blue(c2) - Color.blue(c1)) * t);
        return Color.argb(a, r, g, b);
    }

    @ColorInt
    private int stableColorFromKey(String key) {
        final int h = (key == null) ? 0 : key.hashCode();
        final float hue = (float) (Math.abs(h) % 360);
        final float[] hsv = new float[]{hue, 0.75f, 0.95f};
        return Color.HSVToColor(255, hsv);
    }

    @ColorInt
    private int adjustAlpha(@ColorInt int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * Convert OpenSky "trueTrack" (clockwise from North) into the rotation value used by the
     * osmdroid Marker so that the plane icon nose matches the real heading.
     */
    private static float toMarkerRotation(double trueTrackDeg) {
        if (Double.isNaN(trueTrackDeg) || Double.isInfinite(trueTrackDeg)) return 0f;

        // Normalize to [0, 360)
        double norm = trueTrackDeg % 360.0;
        if (norm < 0) norm += 360.0;

        // osmdroid's rotation is interpreted as counter-clockwise in a number of setups.
        // Using (360 - trueTrack) makes 90° (East) render with the nose pointing East.
        double rot = (360.0 - norm) % 360.0;
        return (float) rot;
    }

    private Drawable getPlaneIcon(@ColorInt int tintColor) {
        Drawable cached = planeIconCache.get(tintColor);
        if (cached != null) return cached;

        Drawable base = AppCompatResources.getDrawable(this, R.drawable.ic_plane);
        if (base == null) return null;

        base = DrawableCompat.wrap(base).mutate();
        DrawableCompat.setTint(base, tintColor);

        Bitmap bmp = drawableToBitmap(base);
        Drawable out = new BitmapDrawable(getResources(), bmp);
        planeIconCache.put(tintColor, out);
        return out;
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        final int w = Math.max(1, drawable.getIntrinsicWidth());
        final int h = Math.max(1, drawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
