package com.irfan.airlinebooking.repository;

import com.irfan.airlinebooking.enums.SeatClass;
import com.irfan.airlinebooking.model.Flight;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC data-access object for {@link Flight}. */
public class FlightRepository extends JdbcRepository implements Repository<Flight, Long> {

    @Override
    public Flight save(Flight flight) {
        String sql = """
                INSERT INTO flights(flight_number, airline, source, destination,
                    departure_time, arrival_time, duration_hours, stops, seat_class, base_fare)
                VALUES(?,?,?,?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, flight);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    flight.setId(keys.getLong(1));
                }
            }
            return flight;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save flight", e);
        }
    }

    private void bind(PreparedStatement ps, Flight f) throws SQLException {
        ps.setString(1, f.getFlightNumber());
        ps.setString(2, f.getAirline());
        ps.setString(3, f.getSource());
        ps.setString(4, f.getDestination());
        ps.setString(5, f.getDepartureTime() == null ? null : f.getDepartureTime().toString());
        ps.setString(6, f.getArrivalTime() == null ? null : f.getArrivalTime().toString());
        ps.setDouble(7, f.getDurationHours());
        ps.setInt(8, f.getStops());
        ps.setString(9, f.getSeatClass().name());
        ps.setDouble(10, f.getBaseFare().doubleValue());
    }

    public Flight update(Flight f) {
        String sql = """
                UPDATE flights SET flight_number=?, airline=?, source=?, destination=?,
                    departure_time=?, arrival_time=?, duration_hours=?, stops=?, seat_class=?, base_fare=?
                WHERE id=?
                """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            bind(ps, f);
            ps.setLong(11, f.getId());
            ps.executeUpdate();
            return f;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update flight", e);
        }
    }

    @Override
    public Optional<Flight> findById(Long id) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM flights WHERE id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load flight", e);
        }
    }

    @Override
    public List<Flight> findAll() {
        return query("SELECT * FROM flights ORDER BY id", null);
    }

    /** Search by optional source / destination / class. Blank values are ignored. */
    public List<Flight> search(String source, String destination, SeatClass seatClass) {
        StringBuilder sql = new StringBuilder("SELECT * FROM flights WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (source != null && !source.isBlank()) {
            sql.append(" AND lower(source)=lower(?)");
            params.add(source);
        }
        if (destination != null && !destination.isBlank()) {
            sql.append(" AND lower(destination)=lower(?)");
            params.add(destination);
        }
        if (seatClass != null) {
            sql.append(" AND seat_class=?");
            params.add(seatClass.name());
        }
        sql.append(" ORDER BY base_fare");
        return query(sql.toString(), params);
    }

    public int count() {
        try (PreparedStatement ps = conn().prepareStatement("SELECT COUNT(*) FROM flights");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to count flights", e);
        }
    }

    private List<Flight> query(String sql, List<Object> params) {
        List<Flight> flights = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            if (params != null) {
                for (int i = 0; i < params.size(); i++) {
                    ps.setObject(i + 1, params.get(i));
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    flights.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Flight query failed", e);
        }
        return flights;
    }

    @Override
    public boolean deleteById(Long id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM flights WHERE id=?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete flight", e);
        }
    }

    private Flight map(ResultSet rs) throws SQLException {
        Flight f = new Flight();
        f.setId(rs.getLong("id"));
        f.setFlightNumber(rs.getString("flight_number"));
        f.setAirline(rs.getString("airline"));
        f.setSource(rs.getString("source"));
        f.setDestination(rs.getString("destination"));
        String dep = rs.getString("departure_time");
        String arr = rs.getString("arrival_time");
        f.setDepartureTime(dep == null ? null : LocalTime.parse(dep));
        f.setArrivalTime(arr == null ? null : LocalTime.parse(arr));
        f.setDurationHours(rs.getDouble("duration_hours"));
        f.setStops(rs.getInt("stops"));
        f.setSeatClass(SeatClass.valueOf(rs.getString("seat_class")));
        f.setBaseFare(BigDecimal.valueOf(rs.getDouble("base_fare")));
        return f;
    }
}
