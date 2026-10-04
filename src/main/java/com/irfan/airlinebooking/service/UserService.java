package com.irfan.airlinebooking.service;

import com.irfan.airlinebooking.exception.InvalidLoginException;
import com.irfan.airlinebooking.exception.ValidationException;
import com.irfan.airlinebooking.model.Admin;
import com.irfan.airlinebooking.model.Passenger;
import com.irfan.airlinebooking.model.User;
import com.irfan.airlinebooking.repository.UserRepository;
import com.irfan.airlinebooking.util.Validator;

import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for registration and authentication.
 * Controllers never talk to the repository directly.
 */
public class UserService {

    private final UserRepository users = new UserRepository();

    /** Registers a new passenger and returns it. */
    public Passenger register(String name, String email, String password, String phone) {
        Validator.requireText(name, "Name");
        String cleanEmail = Validator.requireEmail(email);
        Validator.requireText(password, "Password");

        if (users.findByEmail(cleanEmail).isPresent()) {
            throw new ValidationException("An account with this email already exists");
        }
        Passenger passenger = new Passenger(name.trim(), cleanEmail, password.trim(), phone);
        return (Passenger) users.save(passenger);
    }

    /** Authenticates a user or throws {@link InvalidLoginException}. */
    public User login(String email, String password) {
        Validator.requireText(email, "Email");
        Validator.requireText(password, "Password");
        User user = users.findByEmail(email)
                .orElseThrow(() -> new InvalidLoginException("No account found for " + email));
        if (!user.checkPassword(password)) {
            throw new InvalidLoginException("Incorrect password");
        }
        return user;
    }

    /** Creates the default admin / demo passenger if the table is empty. */
    public void seedDefaults() {
        if (users.isEmpty()) {
            users.save(new Admin("Irfan Admin", "admin@airline.com", "admin123"));
            users.save(new Passenger("Demo Passenger", "demo@airline.com", "demo123", "9999999999"));
        }
    }

    public User findById(Long id) {
        return users.findById(id)
                .orElseThrow(() -> new ValidationException("No user with id " + id));
    }

    /** All passenger accounts (admins excluded). */
    public List<Passenger> passengers() {
        List<Passenger> result = new ArrayList<>();
        for (User u : users.findAll()) {
            if (u instanceof Passenger p) {
                result.add(p);
            }
        }
        return result;
    }

    /** Admin: update a passenger's editable fields. Password left blank = unchanged. */
    public Passenger updatePassenger(Long id, String name, String email, String phone, String password) {
        User user = findById(id);
        if (!(user instanceof Passenger p)) {
            throw new ValidationException("Only passenger accounts can be updated here");
        }
        p.setName(Validator.requireText(name, "Name"));
        p.setEmail(Validator.requireEmail(email));
        p.setPhone(phone);
        if (password != null && !password.isBlank()) {
            p.setPassword(password.trim());
        }
        return (Passenger) users.save(p);
    }
}
