package com.irfan.airlinebooking.repository;

import com.irfan.airlinebooking.util.DbConnection;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates the database schema if it does not exist yet and seeds the default
 * accounts. Safe to run on every startup (all statements use IF NOT EXISTS).
 */
public final class SchemaInitializer {

    private SchemaInitializer() {
    }

    public static void init() {
        try (Statement st = DbConnection.getInstance().getConnection().createStatement()) {
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS users (
                        id       INTEGER PRIMARY KEY AUTOINCREMENT,
                        name     TEXT NOT NULL,
                        email    TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL,
                        role     TEXT NOT NULL,
                        phone    TEXT
                    )
                    """);

            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS flights (
                        id              INTEGER PRIMARY KEY AUTOINCREMENT,
                        flight_number   TEXT NOT NULL,
                        airline         TEXT NOT NULL,
                        source          TEXT NOT NULL,
                        destination     TEXT NOT NULL,
                        departure_time  TEXT,
                        arrival_time    TEXT,
                        duration_hours  REAL NOT NULL,
                        stops           INTEGER NOT NULL,
                        seat_class      TEXT NOT NULL,
                        base_fare       REAL NOT NULL
                    )
                    """);

            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS bookings (
                        id             INTEGER PRIMARY KEY AUTOINCREMENT,
                        pnr            TEXT NOT NULL UNIQUE,
                        user_id        INTEGER NOT NULL,
                        flight_id      INTEGER NOT NULL,
                        flight_number  TEXT,
                        source         TEXT,
                        destination    TEXT,
                        travel_date    TEXT,
                        seat_class     TEXT,
                        seat_number    TEXT,
                        passenger_name TEXT,
                        passenger_age  INTEGER,
                        extra_luggage  INTEGER DEFAULT 0,
                        meals          INTEGER DEFAULT 0,
                        addon_amount   REAL DEFAULT 0,
                        fare           REAL,
                        status         TEXT NOT NULL,
                        booked_at      TEXT,
                        FOREIGN KEY (user_id) REFERENCES users(id),
                        FOREIGN KEY (flight_id) REFERENCES flights(id)
                    )
                    """);

            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS tickets (
                        id             INTEGER PRIMARY KEY AUTOINCREMENT,
                        booking_id     INTEGER NOT NULL,
                        pnr            TEXT,
                        passenger_name TEXT,
                        flight_number  TEXT,
                        source         TEXT,
                        destination    TEXT,
                        travel_date    TEXT,
                        seat_class     TEXT,
                        seat_number    TEXT,
                        extra_luggage  INTEGER DEFAULT 0,
                        meals          INTEGER DEFAULT 0,
                        addon_amount   REAL DEFAULT 0,
                        fare           REAL,
                        issued_at      TEXT,
                        FOREIGN KEY (booking_id) REFERENCES bookings(id)
                    )
                    """);

            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS payments (
                        id           INTEGER PRIMARY KEY AUTOINCREMENT,
                        booking_id   INTEGER NOT NULL,
                        amount       REAL,
                        method       TEXT,
                        status       TEXT,
                        paid_at      TEXT,
                        FOREIGN KEY (booking_id) REFERENCES bookings(id)
                    )
                    """);

            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_flight_route "
                    + "ON flights(source, destination)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_booking_user "
                    + "ON bookings(user_id)");

            // --- migrations for databases created by earlier versions ---
            addColumnIfMissing(st, "bookings", "travel_date", "TEXT");
            addColumnIfMissing(st, "bookings", "extra_luggage", "INTEGER DEFAULT 0");
            addColumnIfMissing(st, "bookings", "meals", "INTEGER DEFAULT 0");
            addColumnIfMissing(st, "bookings", "addon_amount", "REAL DEFAULT 0");
            addColumnIfMissing(st, "tickets", "travel_date", "TEXT");
            addColumnIfMissing(st, "tickets", "seat_class", "TEXT");
            addColumnIfMissing(st, "tickets", "extra_luggage", "INTEGER DEFAULT 0");
            addColumnIfMissing(st, "tickets", "meals", "INTEGER DEFAULT 0");
            addColumnIfMissing(st, "tickets", "addon_amount", "REAL DEFAULT 0");

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize schema", e);
        }
    }

    /** Adds a column to an existing table when it is absent (SQLite migration). */
    private static void addColumnIfMissing(Statement st, String table, String column, String definition)
            throws SQLException {
        try (var rs = st.executeQuery("PRAGMA table_info(" + table + ")")) {
            boolean present = false;
            while (rs.next()) {
                if (column.equalsIgnoreCase(rs.getString("name"))) {
                    present = true;
                    break;
                }
            }
            if (!present) {
                st.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
            }
        }
    }
}
