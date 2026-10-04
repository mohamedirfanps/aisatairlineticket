package com.irfan.airlinebooking.ui;

import com.irfan.airlinebooking.model.User;
import com.irfan.airlinebooking.service.UserService;

/** Handles the welcome / login / registration flow. */
public class LoginScreen {

    private final Console console;
    private final UserService userService;

    public LoginScreen(Console console, UserService userService) {
        this.console = console;
        this.userService = userService;
    }

    /** Runs until the user logs in or chooses to quit. Returns null on quit. */
    public User run() {
        while (true) {
            console.printHeader("AIRLINE BOOKING SYSTEM");
            console.printMenu("Login", "Register as passenger", "Quit");
            int choice = console.readInt("  Choose: ");
            switch (choice) {
                case 1 -> {
                    User user = attemptLogin();
                    if (user != null) {
                        return user;
                    }
                }
                case 2 -> attemptRegister();
                case 3 -> {
                    return null;
                }
                default -> System.out.println("  ! Invalid choice.");
            }
        }
    }

    private User attemptLogin() {
        String email = console.readLine("  Email   : ");
        String password = console.readLine("  Password: ");
        try {
            User user = userService.login(email, password);
            System.out.println("  \u2713 Welcome back, " + user.getName() + "!");
            return user;
        } catch (RuntimeException e) {
            System.out.println("  ! " + e.getMessage());
            console.pause();
            return null;
        }
    }

    private void attemptRegister() {
        System.out.println();
        String name = console.readLine("  Name    : ");
        String email = console.readLine("  Email   : ");
        String password = console.readLine("  Password: ");
        String phone = console.readLine("  Phone   : ");
        try {
            User user = userService.register(name, email, password, phone);
            System.out.println("  \u2713 Registered " + user.getName() + ". You can log in now.");
        } catch (RuntimeException e) {
            System.out.println("  ! " + e.getMessage());
        }
        console.pause();
    }
}
