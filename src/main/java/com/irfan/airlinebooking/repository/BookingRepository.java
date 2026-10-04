package com.irfan.airlinebooking.repository;

import com.irfan.airlinebooking.enums.BookingStatus;
import com.irfan.airlinebooking.enums.SeatClass;
import com.irfan.airlinebooking.model.Booking;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC data-access object for {@link Booking}. */
public class BookingRepository extends JdbcRepository implements Repository<Booking, Long> {

    @Override
    public Booking save(Booking b) {
        String sql = """
                INSERT INTO bookings(pnr, user_id, flight_id, flight_number, source, destination,
                    travel_date, seat_class, seat_number, passenger_name, passenger_age,
                    extra_luggage, meals, addon_amount, fare, status, booked_at)
                VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getPnr());
            ps.setLong(2, b.getUserId());
            ps.setLong(3, b.getFlightId());
            ps.setString(4, b.getFlightNumber());
            ps.setString(5, b.getSource());
            ps.setString(6, b.getDestination());
            ps.setString(7, b.getTravelDate() == null ? null : b.getTravelDate().toString());
            ps.setString(8, b.getSeatClass() == null ? null : b.getSeatClass().name());
            ps.setString(9, b.getSeatNumber());
            ps.setString(10, b.getPassengerName());
            ps.setInt(11, b.getPassengerAge());
            ps.setInt(12, b.isExtraLuggage() ? 1 : 0);
            ps.setInt(13, b.isMeals() ? 1 : 0);
            ps.setDouble(14, b.getAddOnAmount() == null ? 0 : b.getAddOnAmount().doubleValue());
            ps.setDouble(15, b.getFare() == null ? 0 : b.getFare().doubleValue());
            ps.setString(16, b.getStatus().name());
            ps.setString(17, b.getBookedAt() == null ? null : b.getBookedAt().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    b.setId(keys.getLong(1));
                }
            }
            return b;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save booking", e);
        }
    }

    /** Updates the editable passenger / add-on fields (admin only). */
    public void updateEditableFields(Booking b) {
        String sql = """
                UPDATE bookings SET passenger_name=?, passenger_age=?, travel_date=?,
                    seat_class=?, seat_number=?, extra_luggage=?, meals=?, addon_amount=?,
                    fare=?, status=?
                WHERE id=?
                """;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, b.getPassengerName());
            ps.setInt(2, b.getPassengerAge());
            ps.setString(3, b.getTravelDate() == null ? null : b.getTravelDate().toString());
            ps.setString(4, b.getSeatClass() == null ? null : b.getSeatClass().name());
            ps.setString(5, b.getSeatNumber());
            ps.setInt(6, b.isExtraLuggage() ? 1 : 0);
            ps.setInt(7, b.isMeals() ? 1 : 0);
            ps.setDouble(8, b.getAddOnAmount() == null ? 0 : b.getAddOnAmount().doubleValue());
            ps.setDouble(9, b.getFare() == null ? 0 : b.getFare().doubleValue());
            ps.setString(10, b.getStatus().name());
            ps.setLong(11, b.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update booking", e);
        }
    }

    public void updateStatus(Long id, BookingStatus status) {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE bookings SET status=? WHERE id=?")) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update booking status", e);
        }
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return one("SELECT * FROM bookings WHERE id=?", id);
    }

    public Optional<Booking> findByPnr(String pnr) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM bookings WHERE pnr=?")) {
            ps.setString(1, pnr);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load booking", e);
        }
    }

    public List<Booking> findByUser(Long userId) {
        List<Booking> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM bookings WHERE user_id=? ORDER BY id DESC")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list bookings", e);
        }
        return list;
    }

    @Override
    public List<Booking> findAll() {
        List<Booking> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM bookings ORDER BY id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list bookings", e);
        }
        return list;
    }

    /** Counts confirmed bookings of a given flight+seat (any date). */
    public int countBookedSeat(Long flightId, String seatNumber) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT COUNT(*) FROM bookings WHERE flight_id=? AND seat_number=? AND status='CONFIRMED'")) {
            ps.setLong(1, flightId);
            ps.setString(2, seatNumber);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to count seat bookings", e);
        }
    }

    /** Counts confirmed bookings of a given flight+seat on a specific date. */
    public int countBookedSeat(Long flightId, String seatNumber, java.time.LocalDate travelDate) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT COUNT(*) FROM bookings WHERE flight_id=? AND seat_number=? "
                        + "AND travel_date=? AND status='CONFIRMED'")) {
            ps.setLong(1, flightId);
            ps.setString(2, seatNumber);
            ps.setString(3, travelDate == null ? null : travelDate.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to count seat bookings", e);
        }
    }

    /**
     * Highest numeric part across all stored PNRs (e.g. "PNR-1005" -> 1005),
     * or 0 when there are no bookings. Used to keep generated PNRs unique
     * across application restarts.
     */
    public int highestPnrSequence() {
        try (PreparedStatement ps = conn().prepareStatement("SELECT pnr FROM bookings");
             ResultSet rs = ps.executeQuery()) {
            int max = 0;
            while (rs.next()) {
                String pnr = rs.getString(1);
                if (pnr != null && pnr.startsWith("PNR-")) {
                    try {
                        max = Math.max(max, Integer.parseInt(pnr.substring(4)));
                    } catch (NumberFormatException ignored) {
                        // ignore PNRs that don't follow the pattern
                    }
                }
            }
            return max;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to read PNR sequences", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM bookings WHERE id=?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete booking", e);
        }
    }

    private Optional<Booking> one(String sql, Long id) {
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load booking", e);
        }
    }

    private Booking map(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId(rs.getLong("id"));
        b.setPnr(rs.getString("pnr"));
        b.setUserId(rs.getLong("user_id"));
        b.setFlightId(rs.getLong("flight_id"));
        b.setFlightNumber(rs.getString("flight_number"));
        b.setSource(rs.getString("source"));
        b.setDestination(rs.getString("destination"));
        String td = rs.getString("travel_date");
        b.setTravelDate(td == null ? null : java.time.LocalDate.parse(td));
        String sc = rs.getString("seat_class");
        b.setSeatClass(sc == null ? null : SeatClass.valueOf(sc));
        b.setSeatNumber(rs.getString("seat_number"));
        b.setPassengerName(rs.getString("passenger_name"));
        b.setPassengerAge(rs.getInt("passenger_age"));
        b.setExtraLuggage(rs.getInt("extra_luggage") == 1);
        b.setMeals(rs.getInt("meals") == 1);
        b.setAddOnAmount(BigDecimal.valueOf(rs.getDouble("addon_amount")));
        b.setFare(BigDecimal.valueOf(rs.getDouble("fare")));
        b.setStatus(BookingStatus.valueOf(rs.getString("status")));
        String at = rs.getString("booked_at");
        b.setBookedAt(at == null ? null : LocalDateTime.parse(at));
        return b;
    }
}
