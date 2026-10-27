package com.example.planespotter.data;

import android.content.Context;

import com.example.planespotter.model.Plane;
import com.example.planespotter.model.FlightInfo;
import com.example.planespotter.util.Prefs;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * OpenSky REST API client.
 *
 * IMPORTANT:
 * OpenSky moved their REST API authentication to OAuth2. For accounts created after mid‑March 2025,
 * HTTP Basic auth (website username/password) returns 401. You must use an API Client
 * (client_id / client_secret) and obtain a Bearer token.
 */
public class OpenSkyClient {

    private static final String API_BASE = "https://opensky-network.org/api";
    private static final String TOKEN_URL = "https://auth.opensky-network.org/auth/realms/opensky-network/protocol/openid-connect/token";

    private final Context appContext;
    private final OkHttpClient http;
    private final Gson gson = new Gson();

    // In-memory token cache (valid for ~30 minutes)
    private String cachedToken = null;
    private long tokenExpiresAtMs = 0L;

    public OpenSkyClient(Context context) {
        this.appContext = context.getApplicationContext();
        this.http = new OkHttpClient.Builder()
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(12, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Fetch aircraft states within a bounding box.
     */
    public List<Plane> fetchPlanes(double lamin, double lomin, double lamax, double lomax) throws IOException {
        HttpUrl url = HttpUrl.parse(API_BASE + "/states/all").newBuilder()
                .addQueryParameter("lamin", String.valueOf(lamin))
                .addQueryParameter("lomin", String.valueOf(lomin))
                .addQueryParameter("lamax", String.valueOf(lamax))
                .addQueryParameter("lomax", String.valueOf(lomax))
                .build();

        Request.Builder rb = new Request.Builder().url(url).get();

        // Prefer OAuth2 when configured
        if (Prefs.hasOpenSkyApiClient(appContext)) {
            String token = getValidBearerToken();
            if (token != null && !token.isEmpty()) {
                rb.header("Authorization", "Bearer " + token);
            }
        }

        Request req = rb.build();

        try (Response resp = http.newCall(req).execute()) {
            int code = resp.code();
            String body = resp.body() != null ? resp.body().string() : "";

            if (code == 401) {
                // Token invalid/expired or wrong client_id/client_secret
                Prefs.setLastAuthFailedAt(appContext, System.currentTimeMillis());
                throw new IOException("Authentication refused (401). Verify your OpenSky API Client (client_id/client_secret).\n" + body);
            }
            if (code == 429) {
                throw new IOException("Rate limit reached (429). Reduce refresh frequency and/or configure an OpenSky API Client." );
            }
            if (code < 200 || code >= 300) {
                throw new IOException("OpenSky error (HTTP " + code + "): " + body);
            }

            JsonObject root = gson.fromJson(body, JsonObject.class);
            JsonArray states = root != null && root.has("states") && !root.get("states").isJsonNull()
                    ? root.getAsJsonArray("states")
                    : null;

            ArrayList<Plane> out = new ArrayList<>();
            if (states == null) return out;

            for (JsonElement e : states) {
                if (!e.isJsonArray()) continue;
                JsonArray row = e.getAsJsonArray();
                Plane p = Plane.fromOpenSkyRow(row);
                if (p != null) out.add(p);
            }
            return out;
        }
    }

    /**
     * Fetch the most recent flight record for a given aircraft (icao24) within a time window.
     *
     * Notes:
     * - OpenSky may return an empty array depending on availability and account entitlements.
     * - We always fail "softly" at the UI layer; this method throws IOException on HTTP/network problems.
     */
    public FlightInfo fetchLatestFlightForAircraft(String icao24, long beginEpochSec, long endEpochSec) throws IOException {
        HttpUrl url = HttpUrl.parse(API_BASE + "/flights/aircraft").newBuilder()
                .addQueryParameter("icao24", icao24)
                .addQueryParameter("begin", String.valueOf(beginEpochSec))
                .addQueryParameter("end", String.valueOf(endEpochSec))
                .build();

        Request.Builder rb = new Request.Builder().url(url).get();

        if (Prefs.hasOpenSkyApiClient(appContext)) {
            String token = getValidBearerToken();
            if (token != null && !token.isEmpty()) {
                rb.header("Authorization", "Bearer " + token);
            }
        }

        try (Response resp = http.newCall(rb.build()).execute()) {
            int code = resp.code();
            String body = resp.body() != null ? resp.body().string() : "";

            if (code == 401) {
                Prefs.setLastAuthFailedAt(appContext, System.currentTimeMillis());
                throw new IOException("Authentication refused (401) on flights endpoint.\n" + body);
            }
            if (code == 429) {
                throw new IOException("Rate limit reached (429) on flights endpoint.");
            }
            if (code < 200 || code >= 300) {
                throw new IOException("OpenSky flights error (HTTP " + code + "): " + body);
            }

            JsonArray arr = gson.fromJson(body, JsonArray.class);
            if (arr == null || arr.size() == 0) return null;

            // Pick the flight with the most recent lastSeen.
            FlightInfo best = null;
            long bestLastSeen = Long.MIN_VALUE;
            for (JsonElement e : arr) {
                if (!e.isJsonObject()) continue;
                JsonObject o = e.getAsJsonObject();
                FlightInfo fi = FlightInfo.fromJson(o);
                long ls = fi != null ? fi.lastSeen : Long.MIN_VALUE;
                if (fi != null && ls > bestLastSeen) {
                    best = fi;
                    bestLastSeen = ls;
                }
            }
            return best;
        }
    }

    /**
     * Returns a cached OAuth2 Bearer token or fetches a new one.
     */
    private synchronized String getValidBearerToken() throws IOException {
        long now = System.currentTimeMillis();
        // Refresh 60s before expiration
        if (cachedToken != null && now + 60_000L < tokenExpiresAtMs) {
            return cachedToken;
        }
        TokenResponse tr = fetchToken();
        cachedToken = tr.accessToken;
        tokenExpiresAtMs = now + (tr.expiresInSec * 1000L);
        return cachedToken;
    }

    private TokenResponse fetchToken() throws IOException {
        String clientId = Prefs.getOpenSkyClientId(appContext);
        String clientSecret = Prefs.getOpenSkyClientSecret(appContext);

        if (clientId.isEmpty() || clientSecret.isEmpty()) {
            throw new IOException("OpenSky API Client not configured. Please set client_id and client_secret in the Connection screen.");
        }

        RequestBody form = new FormBody.Builder()
                .add("grant_type", "client_credentials")
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .build();

        Request req = new Request.Builder()
                .url(TOKEN_URL)
                .post(form)
                .header("Accept", "application/json")
                .build();

        try (Response resp = http.newCall(req).execute()) {
            int code = resp.code();
            String body = resp.body() != null ? resp.body().string() : "";

            if (code == 401) {
                Prefs.setLastAuthFailedAt(appContext, System.currentTimeMillis());
                throw new IOException("OpenSky token request refused (401). Check client_id/client_secret.\n" + body);
            }
            if (code < 200 || code >= 300) {
                throw new IOException("OpenSky token error (HTTP " + code + "): " + body);
            }

            JsonObject obj = gson.fromJson(body, JsonObject.class);
            if (obj == null || !obj.has("access_token")) {
                throw new IOException("Unexpected token response: " + body);
            }

            String token = obj.get("access_token").getAsString();
            long expiresIn = obj.has("expires_in") ? obj.get("expires_in").getAsLong() : 1800L;

            return new TokenResponse(token, expiresIn);
        }
    }

    private static final class TokenResponse {
        final String accessToken;
        final long expiresInSec;

        TokenResponse(String accessToken, long expiresInSec) {
            this.accessToken = accessToken;
            this.expiresInSec = expiresInSec;
        }
    }
}
