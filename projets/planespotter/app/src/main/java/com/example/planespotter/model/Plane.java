package com.example.planespotter.model;

import com.google.gson.JsonArray;

import java.io.Serializable;

/**
 * Model representing one OpenSky "state vector".
 *
 * The app consumes {@code /api/states/all}. We keep only fields that we display or use for
 * rendering (marker rotation/color, details page, etc.).
 */
public class Plane implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String icao24;
    public final String callsign;
    public final String originCountry;

    public final double latitude;
    public final double longitude;

    /**
     * Barometric altitude in meters, may be null (OpenSky can return null).
     */
    public final Double baroAltitude;

    /**
     * Geometric altitude in meters, may be null.
     */
    public final Double geoAltitude;

    /**
     * True if aircraft is on ground (OpenSky field "on_ground").
     */
    public final boolean onGround;

    /**
     * Track angle in degrees (OpenSky field "true_track").
     */
    public final double trueTrack;

    /**
     * Speed over ground in m/s (OpenSky field "velocity").
     */
    public final double velocity;

    /**
     * Vertical rate in m/s, may be null.
     */
    public final Double verticalRate;

    /**
     * Last contact epoch seconds (OpenSky field "last_contact").
     */
    public final Long lastContact;

    public Plane(
            String icao24,
            String callsign,
            String originCountry,
            double latitude,
            double longitude,
            Double baroAltitude,
            Double geoAltitude,
            boolean onGround,
            double velocity,
            double trueTrack,
            Double verticalRate,
            Long lastContact
    ) {
        this.icao24 = icao24;
        this.callsign = callsign;
        this.originCountry = originCountry;
        this.latitude = latitude;
        this.longitude = longitude;
        this.baroAltitude = baroAltitude;
        this.geoAltitude = geoAltitude;
        this.onGround = onGround;
        this.velocity = velocity;
        this.trueTrack = trueTrack;
        this.verticalRate = verticalRate;
        this.lastContact = lastContact;
    }

    /**
     * Parse one state vector row from OpenSky.
     *
     * Expected order (0-based):
     * 0 icao24
     * 1 callsign
     * 2 origin_country
     * 3 time_position
     * 4 last_contact
     * 5 longitude
     * 6 latitude
     * 7 baro_altitude
     * 8 on_ground
     * 9 velocity
     * 10 true_track
     * 11 vertical_rate
     * 12 sensors
     * 13 geo_altitude
     * 14 squawk
     * 15 spi
     * 16 position_source
     */
    public static Plane fromOpenSkyRow(JsonArray row) {
        String icao = getStr(row, 0);
        String call = getStr(row, 1);
        String country = getStr(row, 2);

        Long lastContact = getLong(row, 4);

        Double lonD = getDouble(row, 5);
        Double latD = getDouble(row, 6);

        Double baroAlt = getDouble(row, 7);
        Boolean onGround = getBool(row, 8);

        Double velocity = getDouble(row, 9);
        Double trueTrack = getDouble(row, 10);
        Double verticalRate = getDouble(row, 11);

        Double geoAlt = getDouble(row, 13);

        // Latitude/longitude are required by the map; if missing, return null to let caller filter.
        if (latD == null || lonD == null) return null;

        return new Plane(
                icao,
                call,
                country,
                latD,
                lonD,
                baroAlt,
                geoAlt,
                onGround != null && onGround,
                velocity != null ? velocity : 0.0,
                trueTrack != null ? trueTrack : 0.0,
                verticalRate,
                lastContact
        );
    }

    /**
     * Best-effort altitude in meters (geo preferred, otherwise baro).
     */
    public Double bestAltitudeMeters() {
        if (geoAltitude != null) return geoAltitude;
        return baroAltitude;
    }

    private static String getStr(JsonArray row, int idx) {
        if (row == null || row.size() <= idx || row.get(idx).isJsonNull()) return null;
        return row.get(idx).getAsString().trim();
    }

    private static Double getDouble(JsonArray row, int idx) {
        if (row == null || row.size() <= idx || row.get(idx).isJsonNull()) return null;
        try {
            return row.get(idx).getAsDouble();
        } catch (Exception ignore) {
            return null;
        }
    }

    private static Long getLong(JsonArray row, int idx) {
        if (row == null || row.size() <= idx || row.get(idx).isJsonNull()) return null;
        try {
            return row.get(idx).getAsLong();
        } catch (Exception ignore) {
            return null;
        }
    }

    private static Boolean getBool(JsonArray row, int idx) {
        if (row == null || row.size() <= idx || row.get(idx).isJsonNull()) return null;
        try {
            return row.get(idx).getAsBoolean();
        } catch (Exception ignore) {
            return null;
        }
    }
}
