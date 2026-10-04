package com.irfan.airlinebooking.repository;

import com.irfan.airlinebooking.util.DbConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;

/** Shared JDBC plumbing for the concrete repositories. */
abstract class JdbcRepository {

    protected Connection conn() {
        return DbConnection.getInstance().getConnection();
    }

    protected void setNullableLong(PreparedStatement ps, int index, Long value) throws SQLException {
        if (value == null) {
            ps.setNull(index, java.sql.Types.INTEGER);
        } else {
            ps.setLong(index, value);
        }
    }
}
