package com.irfan.airlinebooking.util;

/**
 * Central place for runtime configuration.
 * <p>
 * Resolution order for each value:
 * <ol>
 *   <li>JVM system property, e.g. {@code -Dairline.dbUrl=...}</li>
 *   <li>Environment variable, e.g. {@code AIRLINE_DBURL=...}</li>
 *   <li>Built-in default</li>
 * </ol>
 * This lets hosting platforms configure the app purely through env vars
 * (which is the common convention) while tests and local runs keep using
 * system properties.
 */
public final class Config {

    private Config() {
    }

    /** JDBC url of the SQLite database file. */
    public static String dbUrl() {
        return resolve("airline.dbUrl", "AIRLINE_DBURL", "jdbc:sqlite:data/airline.db");
    }

    private static String resolve(String sysProp, String envVar, String fallback) {
        String value = System.getProperty(sysProp);
        if (value == null || value.isBlank()) {
            value = System.getenv(envVar);
        }
        return value == null || value.isBlank() ? fallback : value;
    }
}
