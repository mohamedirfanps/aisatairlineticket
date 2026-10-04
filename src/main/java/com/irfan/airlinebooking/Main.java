package com.irfan.airlinebooking;

import com.irfan.airlinebooking.model.Admin;
import com.irfan.airlinebooking.model.Passenger;
import com.irfan.airlinebooking.model.User;
import com.irfan.airlinebooking.repository.SchemaInitializer;
import com.irfan.airlinebooking.service.FlightService;
import com.irfan.airlinebooking.service.UserService;
import com.irfan.airlinebooking.ui.AdminMenu;
import com.irfan.airlinebooking.ui.Console;
import com.irfan.airlinebooking.ui.LoginScreen;
import com.irfan.airlinebooking.ui.PassengerMenu;

/**
 * Application entry point.
 * <p>
 * On startup it creates the schema (if needed), seeds the default accounts,
 * and imports the flight dataset the first time the app runs.
 */
public class Main {

    public static void main(String[] args) {
        Console console = new Console();
        UserService userService = new UserService();
        FlightService flightService = new FlightService();

        SchemaInitializer.init();
        userService.seedDefaults();

        // One-time network generation so the demo starts with flights.
        if (flightService.count() == 0) {
            System.out.println("First run: generating flight network, please wait...");
            try {
                int inserted = flightService.seedNetwork();
                System.out.println("Generated " + inserted + " flights.");
            } catch (RuntimeException e) {
                System.out.println("Flight network generation skipped: " + e.getMessage());
            }
        }

        LoginScreen loginScreen = new LoginScreen(console, userService);
        while (true) {
            User user = loginScreen.run();
            if (user == null) {
                System.out.println("Goodbye!");
                return;
            }
            if (user instanceof Admin admin) {
                new AdminMenu(console, admin).show();
            } else if (user instanceof Passenger passenger) {
                new PassengerMenu(console, passenger).show();
            }
        }
    }
}
