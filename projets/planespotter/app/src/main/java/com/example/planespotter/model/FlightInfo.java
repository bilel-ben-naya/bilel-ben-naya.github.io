package com.example.planespotter.model;

import com.google.gson.JsonObject;

import java.io.Serializable;

/**
 * Minimal flight information from OpenSky /api/flights/aircraft.
 * Fields are optional depending on availability.
 */
public class FlightInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    public String callsign;
    public String estDepartureAirport;
    public String estArrivalAirport;
    public long firstSeen;
    public long lastSeen;

    public static FlightInfo fromJsonObject(JsonObject o) {
        FlightInfo f = new FlightInfo();
        f.callsign = optString(o, "callsign");
        f.estDepartureAirport = optString(o, "estDepartureAirport");
        f.estArrivalAirport = optString(o, "estArrivalAirport");
        f.firstSeen = optLong(o, "firstSeen");
        f.lastSeen = optLong(o, "lastSeen");
        return f;
    }

    private static String optString(JsonObject o, String key) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) return null;
        String s = o.get(key).getAsString();
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private static long optLong(JsonObject o, String key) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) return 0L;
        try {
            return o.get(key).getAsLong();
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * Backward compatible alias.
     *
     * Earlier iterations of the code called {@code fromJson(...)}.
     */
    public static FlightInfo fromJson(JsonObject o) {
        return fromJsonObject(o);
    }
}
