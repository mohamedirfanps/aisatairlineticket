package com.irfan.airlinebooking.ui;

import com.irfan.airlinebooking.enums.SeatClass;
import com.irfan.airlinebooking.model.Admin;
import com.irfan.airlinebooking.model.Flight;
import com.irfan.airlinebooking.service.FlightService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Admin-facing console menu for flight management and the one-time import. */
public class AdminMenu {

    private final Console console;
    private final Admin admin;
    private final FlightService flightService = new FlightService();

    public AdminMenu(Console console, Admin admin) {
        this.console = console;
        this.admin = admin;
    }

    public void show() {
        boolean running = true;
        while (running) {
            console.printHeader("ADMIN - " + admin.getName());
            console.printMenu(
                    "List flights",
                    "Add flight",
                    "Update flight",
                    "Delete flight",
                    "Import dataset (one-time)",
                    "Logout");
            int choice = console.readInt("  Choose: ");
            switch (choice) {
                case 1 -> listFlights();
                case 2 -> addFlight();
                case 3 -> updateFlight();
                case 4 -> deleteFlight();
                case 5 -> importDataset();
                case 6 -> running = false;
                default -> System.out.println("  ! Invalid choice.");
            }
        }
    }

    private void listFlights() {
        List<Flight> flights = flightService.findAll();
        System.out.println("  Total flights: " + flights.size());
        int shown = 0;
        for (Flight f : flights) {
            System.out.printf("  #%-5d %s%n", f.getId(), f);
            if (++shown >= 40) {
                System.out.println("  ... (showing first 40)");
                break;
            }
        }
        console.pause();
    }

    private void addFlight() {
        try {
            String number = console.readLine("  Flight number: ");
            String airline = console.readLine("  Airline      : ");
            String source = console.readLine("  Source city  : ");
            String destination = console.readLine("  Dest city    : ");
            double duration = Double.parseDouble(console.readLine("  Duration (h): "));
            int stops = console.readInt("  Stops (0/1)  : ");
            SeatClass seatClass = readSeatClass();
            double base = Double.parseDouble(console.readLine("  Base fare    : "));

            LocalTime departure = LocalTime.of(9, 0);
            LocalTime arrival = departure.plusMinutes(Math.round(duration * 60));

            Flight flight = new Flight(number, airline, source, destination,
                    departure, arrival, duration, stops, seatClass, BigDecimal.valueOf(base));
            flightService.addFlight(flight);
            System.out.println("  \u2713 Flight added with id " + flight.getId());
        } catch (RuntimeException e) {
            System.out.println("  ! " + e.getMessage());
        }
        console.pause();
    }

    private void updateFlight() {
        long id = console.readInt("  Flight id to update: ");
        if (!flightService.exists(id)) {
            System.out.println("  ! No such flight.");
            console.pause();
            return;
        }
        Flight f = flightService.findById(id);
        System.out.println("  Editing: " + f);
        f.setAirline(orDefault(console.readLine("  Airline [" + f.getAirline() + "]: "), f.getAirline()));
        f.setBaseFare(BigDecimal.valueOf(Double.parseDouble(
                orDefault(console.readLine("  Base fare [" + f.getBaseFare() + "]: "),
                        f.getBaseFare().toString()))));
        flightService.updateFlight(f);
        System.out.println("  \u2713 Updated.");
        console.pause();
    }

    private String orDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private void deleteFlight() {
        long id = console.readInt("  Flight id to delete: ");
        System.out.println(flightService.deleteFlight(id) ? "  \u2713 Deleted." : "  ! Not found.");
        console.pause();
    }

    private void importDataset() {
        System.out.println("  This generates the full flight network once.");
        if (flightService.count() > 0) {
            System.out.println("  ! Flights already exist (" + flightService.count()
                    + "). Delete data/airline.db to regenerate.");
            console.pause();
            return;
        }
        try {
            int inserted = flightService.seedNetwork();
            System.out.println("  \u2713 Generated " + inserted + " flights.");
        } catch (RuntimeException e) {
            System.out.println("  ! Generation failed: " + e.getMessage());
        }
        console.pause();
    }

    private SeatClass readSeatClass() {
        console.printMenu("Economy", "Business");
        return console.readInt("  Class: ") == 2 ? SeatClass.BUSINESS : SeatClass.ECONOMY;
    }
}
