package com.example.planespotter.data;

import java.io.IOException;

/**
 * Typed HTTP error for OpenSky API calls.
 *
 * We keep this as an IOException so existing caller code that catches IOException
 * continues to work.
 */
public class OpenSkyHttpException extends IOException {
    private final int code;

    public OpenSkyHttpException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
