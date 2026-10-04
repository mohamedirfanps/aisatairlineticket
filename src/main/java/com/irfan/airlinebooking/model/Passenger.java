package com.irfan.airlinebooking.model;

import com.irfan.airlinebooking.enums.Role;

/** A passenger: searches flights and makes bookings. */
public class Passenger extends User {
    private String phone;

    public Passenger() {
        super();
    }

    public Passenger(String name, String email, String password, String phone) {
        super(name, email, password);
        this.phone = phone;
    }

    @Override
    public Role getRole() {
        return Role.PASSENGER;
    }

    @Override
    public String describe() {
        return "Passenger[" + getName() + " <" + getEmail() + ">]";
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
