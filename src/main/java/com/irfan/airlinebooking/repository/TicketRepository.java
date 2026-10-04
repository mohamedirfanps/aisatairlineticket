package com.irfan.airlinebooking.repository;

import com.irfan.airlinebooking.model.Ticket;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC data-access object for {@link Ticket}. */
public class TicketRepository extends JdbcRepository implements Repository<Ticket, Long> {

    @Override
    public Ticket save(Ticket t) {
        String sql = """
                INSERT INTO tickets(booking_id, pnr, passenger_name, flight_number, source,
                    destination, travel_date, seat_class, seat_number, extra_luggage, meals,
                    addon_amount, fare, issued_at)
                VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, t.getBookingId());
            ps.setString(2, t.getPnr());
            ps.setString(3, t.getPassengerName());
            ps.setString(4, t.getFlightNumber());
            ps.setString(5, t.getSource());
            ps.setString(6, t.getDestination());
            ps.setString(7, t.getTravelDate() == null ? null : t.getTravelDate().toString());
            ps.setString(8, t.getSeatClass() == null ? null : t.getSeatClass().name());
            ps.setString(9, t.getSeatNumber());
            ps.setInt(10, t.isExtraLuggage() ? 1 : 0);
            ps.setInt(11, t.isMeals() ? 1 : 0);
            ps.setDouble(12, t.getAddOnAmount() == null ? 0 : t.getAddOnAmount().doubleValue());
            ps.setDouble(13, t.getFare() == null ? 0 : t.getFare().doubleValue());
            ps.setString(14, t.getIssuedAt() == null ? null : t.getIssuedAt().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    t.setId(keys.getLong(1));
                }
            }
            return t;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save ticket", e);
        }
    }

    @Override
    public Optional<Ticket> findById(Long id) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM tickets WHERE id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load ticket", e);
        }
    }

    public Optional<Ticket> findByPnr(String pnr) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM tickets WHERE pnr=?")) {
            ps.setString(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load ticket", e);
        }
    }

    @Override
    public List<Ticket> findAll() {
        List<Ticket> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM tickets ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list tickets", e);
        }
        return list;
    }

    @Override
    public boolean deleteById(Long id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM tickets WHERE id=?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete ticket", e);
        }
    }

    private Ticket map(ResultSet rs) throws SQLException {
        Ticket t = new Ticket();
        t.setId(rs.getLong("id"));
        t.setBookingId(rs.getLong("booking_id"));
        t.setPnr(rs.getString("pnr"));
        t.setPassengerName(rs.getString("passenger_name"));
        t.setFlightNumber(rs.getString("flight_number"));
        t.setSource(rs.getString("source"));
        t.setDestination(rs.getString("destination"));
        String td = rs.getString("travel_date");
        t.setTravelDate(td == null ? null : java.time.LocalDate.parse(td));
        String sc = rs.getString("seat_class");
        t.setSeatClass(sc == null ? null : com.irfan.airlinebooking.enums.SeatClass.valueOf(sc));
        t.setSeatNumber(rs.getString("seat_number"));
        t.setExtraLuggage(rs.getInt("extra_luggage") == 1);
        t.setMeals(rs.getInt("meals") == 1);
        t.setAddOnAmount(BigDecimal.valueOf(rs.getDouble("addon_amount")));
        t.setFare(BigDecimal.valueOf(rs.getDouble("fare")));
        String at = rs.getString("issued_at");
        t.setIssuedAt(at == null ? null : LocalDateTime.parse(at));
        return t;
    }
}
