package com.irfan.airlinebooking.repository;

import com.irfan.airlinebooking.enums.Role;
import com.irfan.airlinebooking.model.Admin;
import com.irfan.airlinebooking.model.Passenger;
import com.irfan.airlinebooking.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** JDBC data-access object for {@link User} and its subclasses. */
public class UserRepository extends JdbcRepository implements Repository<User, Long> {

    @Override
    public User save(User user) {
        if (user.isNew()) {
            return insert(user);
        }
        return update(user);
    }

    private User insert(User user) {
        String sql = "INSERT INTO users(name, email, password, role, phone) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole().name());
            ps.setString(5, user instanceof Passenger p ? p.getPhone() : null);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }
            return user;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to save user: " + e.getMessage(), e);
        }
    }

    private User update(User user) {
        String sql = "UPDATE users SET name=?, email=?, password=?, role=?, phone=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole().name());
            ps.setString(5, user instanceof Passenger p ? p.getPhone() : null);
            ps.setLong(6, user.getId());
            ps.executeUpdate();
            return user;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update user: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load user", e);
        }
    }

    /** Finds a user by (case-insensitive) email. */
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE lower(email)=lower(?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to load user", e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY id";
        List<User> users = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(map(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list users", e);
        }
        return users;
    }

    @Override
    public boolean deleteById(Long id) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM users WHERE id=?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to delete user", e);
        }
    }

    /** True when no users exist yet (used to seed defaults). */
    public boolean isEmpty() {
        try (PreparedStatement ps = conn().prepareStatement("SELECT COUNT(*) FROM users");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() && rs.getInt(1) == 0;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to count users", e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        Role role = Role.valueOf(rs.getString("role"));
        User user;
        if (role == Role.ADMIN) {
            user = new Admin();
        } else {
            Passenger passenger = new Passenger();
            passenger.setPhone(rs.getString("phone"));
            user = passenger;
        }
        user.setId(rs.getLong("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        return user;
    }
}
