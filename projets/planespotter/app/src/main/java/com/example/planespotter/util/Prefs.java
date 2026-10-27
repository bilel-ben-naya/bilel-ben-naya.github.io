package com.example.planespotter.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Shared preferences helper.
 *
 * OpenSky note:
 * - OpenSky REST API authentication has transitioned to OAuth2.
 * - For accounts created after mid-March 2025, HTTP Basic auth with website username/password returns 401.
 * - You must create an "API client" (client_id / client_secret) in your OpenSky account and use those
 *   values in this app.
 */
public final class Prefs {
    private Prefs() {}

    /** Default refresh interval (seconds) used when the user has not configured it. */
    public static final int DEFAULT_INTERVAL_SEC = 12;
    /** Default bounding box half-size (degrees) used when the user has not configured it. */
    public static final double DEFAULT_DELTA_DEG = 0.40;
    /** Default maximum number of planes displayed on the map. */
    public static final int DEFAULT_MAX_PLANES = 250;

    private static final String FILE = "planespotter_prefs";

    private static final String KEY_INTERVAL_SEC = "interval_sec";
    private static final String KEY_DELTA_DEG_BITS = "delta_deg_bits";

    // Keep the historical keys for backward compatibility with existing installs.
    // Previous versions stored website username/password as opensky_user/opensky_pass.
    // We now store client_id/client_secret in the same keys.
    private static final String KEY_OS_CLIENT_ID = "opensky_user";
    private static final String KEY_OS_CLIENT_SECRET = "opensky_pass";

    // Safety valve: maximum number of planes displayed on the map.
    // Keeps the UI responsive and helps avoid excessive API usage.
    private static final String KEY_MAX_PLANES = "max_planes";

    private static final String KEY_LAST_AUTH_FAILED_AT = "last_auth_failed_at";

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static int getIntervalSec(Context c) {
        return sp(c).getInt(KEY_INTERVAL_SEC, DEFAULT_INTERVAL_SEC);
    }

    public static void setIntervalSec(Context c, int sec) {
        int v = Math.max(5, Math.min(120, sec));
        sp(c).edit().putInt(KEY_INTERVAL_SEC, v).apply();
    }

    public static double getDeltaDeg(Context c) {
        long bits = sp(c).getLong(KEY_DELTA_DEG_BITS, Double.doubleToRawLongBits(DEFAULT_DELTA_DEG));
        return Double.longBitsToDouble(bits);
    }

    public static void setDeltaDeg(Context c, double deg) {
        double v = Math.max(0.05, Math.min(5.0, deg));
        sp(c).edit().putLong(KEY_DELTA_DEG_BITS, Double.doubleToRawLongBits(v)).apply();
    }

    public static String getOpenSkyClientId(Context c) {
        return sp(c).getString(KEY_OS_CLIENT_ID, "");
    }

    public static String getOpenSkyClientSecret(Context c) {
        return sp(c).getString(KEY_OS_CLIENT_SECRET, "");
    }

    public static void setOpenSkyApiClient(Context c, String clientId, String clientSecret) {
        sp(c).edit()
                .putString(KEY_OS_CLIENT_ID, clientId != null ? clientId.trim() : "")
                .putString(KEY_OS_CLIENT_SECRET, clientSecret != null ? clientSecret : "")
                .apply();
    }

    public static boolean hasOpenSkyApiClient(Context c) {
        String id = getOpenSkyClientId(c);
        String secret = getOpenSkyClientSecret(c);
        return id != null && !id.trim().isEmpty() && secret != null && !secret.isEmpty();
    }

    public static long getLastAuthFailedAt(Context c) {
        return sp(c).getLong(KEY_LAST_AUTH_FAILED_AT, 0L);
    }

    public static void setLastAuthFailedAt(Context c, long epochMs) {
        sp(c).edit().putLong(KEY_LAST_AUTH_FAILED_AT, epochMs).apply();
    }

    public static int getMaxPlanes(Context c) {
        // Default: a reasonable upper bound that keeps the map smooth on mid-range devices.
        return sp(c).getInt(KEY_MAX_PLANES, DEFAULT_MAX_PLANES);
    }

    public static void setMaxPlanes(Context c, int maxPlanes) {
        // Hard bounds to prevent out-of-range values from freezing the UI.
        int v = Math.max(25, Math.min(2000, maxPlanes));
        sp(c).edit().putInt(KEY_MAX_PLANES, v).apply();
    }

    // Legacy wrappers (avoid breaking older code paths).
    @Deprecated
    public static String getOpenSkyUser(Context c) {
        return getOpenSkyClientId(c);
    }

    @Deprecated
    public static String getOpenSkyPass(Context c) {
        return getOpenSkyClientSecret(c);
    }

    @Deprecated
    public static void setOpenSkyCredentials(Context c, String user, String pass) {
        setOpenSkyApiClient(c, user, pass);
    }
}
