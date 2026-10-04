package com.irfan.airlinebooking.model;

import com.irfan.airlinebooking.enums.Role;

/** An administrator: manages flights and views reports. */
public class Admin extends User {

    public Admin() {
        super();
    }

    public Admin(String name, String email, String password) {
        super(name, email, password);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

    @Override
    public String describe() {
        return "Admin[" + getName() + "]";
    }
}
