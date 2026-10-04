package com.irfan.airlinebooking.model;

import com.irfan.airlinebooking.enums.Role;

/**
 * Abstract base for everyone who can log in.
 * <p>
 * Demonstrates <b>abstraction</b> (abstract class + abstract method) and
 * <b>encapsulation</b> (private password). {@link Admin} and {@link Passenger}
 * provide concrete behaviour via <b>inheritance</b>.
 */
public abstract class User extends BaseEntity {
    private String name;
    private String email;
    private String password;

    protected User() {
    }

    protected User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    /** Role is decided by the concrete subclass. */
    public abstract Role getRole();

    /** Short label used by the UI. */
    public abstract String describe();

    public boolean checkPassword(String raw) {
        return password != null && password.equals(raw);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return describe();
    }
}
