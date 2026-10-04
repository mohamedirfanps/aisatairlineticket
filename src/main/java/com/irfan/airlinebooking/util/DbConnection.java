package com.irfan.airlinebooking.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton JDBC connection factory (design pattern: <b>Singleton</b>).
 * <p>
 * SQLite is embedded and file based, so a single application-wide connection
 * is enough for a desktop demo. The connection is created lazily and pinned
 * to foreign-key enforcement.
 */
public final class DbConnection {

    private static DbConnection instance;
    private Connection connection;
    private String currentUrl;

    private DbConnection() {
        connect();
    }

    /** Thread-safe lazy singleton. */
    public static synchronized DbConnection getInstance() {
        if (instance == null) {
            instance = new DbConnection();
        }
        return instance;
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            currentUrl = Config.dbUrl();
            connection = DriverManager.getConnection(currentUrl);
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("SQLite JDBC driver not on classpath", e);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot open database: " + Config.dbUrl(), e);
        }
    }

    public Connection getConnection() {
        try {
            // Reconnect if the connection dropped *or* the configured URL changed
            // (useful when tests point at a different database).
            boolean urlChanged = !Config.dbUrl().equals(currentUrl);
            if (connection == null || connection.isClosed() || urlChanged) {
                connect();
            }
        } catch (SQLException e) {
            connect();
        }
        return connection;
    }

    public synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
                // nothing useful to do on shutdown
            } finally {
                connection = null;
                instance = null;
            }
        }
    }
}
