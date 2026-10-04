package com.irfan.airlinebooking.repository;

import com.irfan.airlinebooking.enums.PaymentStatus;
import com.irfan.airlinebooking.model.CardPayment;
import com.irfan.airlinebooking.model.NetBankingPayment;
import com.irfan.airlinebooking.model.Payment;
import com.irfan.airlinebooking.model.UpiPayment;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC data-access object for {@link Payment}.
 * <p>
 * Only the shared fields are persisted; on read the {@code method} column is
 * used to rebuild the correct subclass, which is enough for displaying
 * receipts and reports.
 */
public class PaymentRepository extends JdbcRepository implements Repository<Payment, Long> {

    @Override
    public Payment save(Payment payment) {
        return save(payment, payment.getBookingId());
    }

    /** Persists a payment against a booking id. */
    public Payment save(Payment payment, Long bookingId) {
        String sql = """
                INSERT INTO payments(booking_id, amount, method, status, paid_at)
                VALUES(?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, bookingId);
            ps.setDouble(2, payment.getAmount() == null ? 0 : payment.getAmount().doubleValue());
            ps.setString(3, payment.describeMethod());
            ps.setString(4, payment.getStatus() == null ? null : payment.getStatus().name());
            ps.setString(5, payment.getPaidAt() == null ? null : payment.getPaidAt().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    payment.setId(keys.getLong(1));
                }
            }
            payment.setBookingId(bookingId);
            return payment;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save payment", e);
        }
    }

    /** Returns the most recent payment recorded for a booking. */
    public Optional<Payment> findByBooking(Long bookingId) {
        String sql = "SELECT * FROM payments WHERE booking_id=? ORDER BY id DESC LIMIT 1";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load payment", e);
        }
    }

    @Override
    public Optional<Payment> findById(Long id) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM payments WHERE id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load payment", e);
        }
    }

    @Override
    public List<Payment> findAll() {
        List<Payment> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM payments ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list payments", e);
        }
        return list;
    }

    @Override
    public boolean deleteById(Long id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM payments WHERE id=?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete payment", e);
        }
    }

    private Payment map(ResultSet rs) throws SQLException {
        String method = rs.getString("method");
        Payment payment = switch (method == null ? "" : method) {
            case "UPI" -> new UpiPayment();
            case "Card" -> new CardPayment();
            case "NetBanking" -> new NetBankingPayment();
            default -> new UpiPayment();
        };
        payment.setId(rs.getLong("id"));
        payment.setBookingId(rs.getLong("booking_id"));
        payment.setAmount(BigDecimal.valueOf(rs.getDouble("amount")));
        String status = rs.getString("status");
        payment.setStatus(status == null ? null : PaymentStatus.valueOf(status));
        String at = rs.getString("paid_at");
        payment.setPaidAt(at == null ? null : LocalDateTime.parse(at));
        return payment;
    }
}
