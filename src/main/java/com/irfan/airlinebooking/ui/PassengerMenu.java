package com.irfan.airlinebooking.ui;

import com.irfan.airlinebooking.enums.SeatClass;
import com.irfan.airlinebooking.model.Booking;
import com.irfan.airlinebooking.model.CardPayment;
import com.irfan.airlinebooking.model.Flight;
import com.irfan.airlinebooking.model.NetBankingPayment;
import com.irfan.airlinebooking.model.Passenger;
import com.irfan.airlinebooking.model.Payment;
import com.irfan.airlinebooking.model.Seat;
import com.irfan.airlinebooking.model.Ticket;
import com.irfan.airlinebooking.model.UpiPayment;
import com.irfan.airlinebooking.service.BookingService;
import com.irfan.airlinebooking.service.FlightService;
import com.irfan.airlinebooking.service.SeatService;
import com.irfan.airlinebooking.util.Airports;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Passenger-facing console menu. */
public class PassengerMenu {

    private final Console console;
    private final Passenger passenger;
    private final FlightService flightService = new FlightService();
    private final BookingService bookingService = new BookingService();
    private final SeatService seatService = new SeatService();

    public PassengerMenu(Console console, Passenger passenger) {
        this.console = console;
        this.passenger = passenger;
    }

    public void show() {
        boolean running = true;
        while (running) {
            console.printHeader("PASSENGER - " + passenger.getName());
            console.printMenu(
                    "Search & book a flight",
                    "My bookings",
                    "View ticket by PNR",
                    "Cancel a booking",
                    "Logout");
            int choice = console.readInt("  Choose: ");
            switch (choice) {
                case 1 -> searchAndBook();
                case 2 -> myBookings();
                case 3 -> viewTicket();
                case 4 -> cancelBooking();
                case 5 -> running = false;
                default -> System.out.println("  ! Invalid choice.");
            }
        }
    }

    private void searchAndBook() {
        List<String> cities = Airports.all().stream().map(a -> a.getCity()).toList();
        System.out.println("  Cities: " + String.join(", ", cities));
        String source = console.readLine("  From (blank = any)     : ");
        String destination = console.readLine("  To   (blank = any)     : ");
        SeatClass seatClass = readSeatClass();

        List<Flight> results = flightService.search(source, destination, seatClass);
        if (results.isEmpty()) {
            System.out.println("  ! No flights match your search.");
            console.pause();
            return;
        }
        showFlightList(results);

        int index = console.readInt("  Select flight # (0 to go back): ");
        if (index <= 0 || index > results.size()) {
            return;
        }
        Flight flight = results.get(index - 1);
        bookFlight(flight);
    }

    private void showFlightList(List<Flight> flights) {
        System.out.println();
        System.out.printf("  %-3s %-10s %-12s %-10s %-8s %-8s %-7s %-12s%n",
                "#", "Airline", "Flight", "Route", "Dep", "Arr", "Dur", "Base(INR)");
        for (int i = 0; i < flights.size(); i++) {
            Flight f = flights.get(i);
            System.out.printf("  %-3d %-10s %-12s %-10s %-8s %-8s %-6.1fh %-12.2f%n",
                    i + 1, f.getAirline(), f.getFlightNumber(),
                    f.getSource().substring(0, 3) + "-" + f.getDestination().substring(0, 3),
                    f.getDepartureTime() == null ? "?" : f.getDepartureTime().toString().substring(0, 5),
                    f.getArrivalTime() == null ? "?" : f.getArrivalTime().toString().substring(0, 5),
                    f.getDurationHours(), f.getBaseFare().doubleValue());
        }
        System.out.println();
    }

    private void bookFlight(Flight flight) {
        List<Seat> seats = seatService.seatMap(flight.getSeatClass());
        System.out.println("  Class: " + flight.getSeatClass());
        printSeatMap(seats, flight.getId());

        String seatNumber = console.readLine("  Seat number (e.g. 2A): ").toUpperCase();
        Seat selected = seats.stream()
                .filter(s -> s.getSeatNumber().equalsIgnoreCase(seatNumber))
                .findFirst()
                .orElse(null);
        if (selected == null) {
            System.out.println("  ! That seat does not exist.");
            console.pause();
            return;
        }
        if (bookingService.isSeatTaken(flight.getId(), selected.getSeatNumber())) {
            System.out.println("  ! Seat already booked. Pick another.");
            console.pause();
            return;
        }

        String passengerName = console.readLine("  Passenger name: ");
        int age = console.readInt("  Passenger age : ");

        BigDecimal fare = selected.calculateFare(flight.getBaseFare().doubleValue());
        System.out.printf("  Fare for %s %s: INR %.2f%n", flight.getSeatClass(),
                selected.getSeatNumber(), fare.doubleValue());

        Payment payment = buildPayment(fare);
        if (payment == null) {
            System.out.println("  ! Payment cancelled.");
            console.pause();
            return;
        }

        try {
            Ticket ticket = bookingService.bookPassenger(
                    flight, passenger.getId(), selected, passengerName, age, payment);
            System.out.println();
            System.out.println(ticket.render());
        } catch (RuntimeException e) {
            System.out.println("  ! Booking failed: " + e.getMessage());
        }
        console.pause();
    }

    private void printSeatMap(List<Seat> seats, Long flightId) {
        System.out.println("  Seat map  (X = booked)");
        seats.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        s -> s.getSeatNumber().charAt(0),
                        java.util.LinkedHashMap::new,
                        java.util.stream.Collectors.toList()))
                .forEach((row, rowSeats) -> {
                    StringBuilder sb = new StringBuilder("    ");
                    for (Seat s : rowSeats) {
                        boolean taken = bookingService.isSeatTaken(flightId, s.getSeatNumber());
                        sb.append(String.format("%-5s", taken ? "X" : s.getSeatNumber()));
                    }
                    System.out.println(sb);
                });
        System.out.println();
    }

    private Payment buildPayment(BigDecimal fare) {
        console.printMenu("UPI", "Card", "Net banking", "Cancel");
        int choice = console.readInt("  Payment method: ");
        return switch (choice) {
            case 1 -> new UpiPayment(fare, console.readLine("  UPI id: "));
            case 2 -> new CardPayment(fare,
                    console.readLine("  16-digit card number: "),
                    console.readLine("  Card holder name    : "));
            case 3 -> new NetBankingPayment(fare, console.readLine("  Bank name: "));
            default -> null;
        };
    }

    private void myBookings() {
        List<Booking> bookings = bookingService.bookingsForUser(passenger.getId());
        List<String> rows = new ArrayList<>();
        for (Booking b : bookings) {
            rows.add(String.format("  %-10s %-10s %-12s %-6s %-10.2f %s",
                    b.getPnr(), b.getSource() + "->" + b.getDestination(),
                    b.getSeatClass() + " " + b.getSeatNumber(),
                    b.getStatus(), b.getFare().doubleValue(),
                    b.getBookedAt()));
        }
        console.printTable("  PNR        Route         Seat         Status Fare(INR)  Booked", rows);
        console.pause();
    }

    private void viewTicket() {
        String pnr = console.readLine("  PNR: ");
        try {
            System.out.println(bookingService.ticketForPnr(pnr).render());
        } catch (RuntimeException e) {
            System.out.println("  ! " + e.getMessage());
        }
        console.pause();
    }

    private void cancelBooking() {
        String pnr = console.readLine("  PNR to cancel: ");
        try {
            Booking b = bookingService.cancel(pnr, passenger.getId(), false);
            System.out.println("  \u2713 Booking " + b.getPnr() + " cancelled. Refund of INR "
                    + b.getFare().doubleValue() + " initiated (simulated).");
        } catch (RuntimeException e) {
            System.out.println("  ! " + e.getMessage());
        }
        console.pause();
    }

    private SeatClass readSeatClass() {
        console.printMenu("Economy", "Business", "Any");
        int c = console.readInt("  Class: ");
        return switch (c) {
            case 1 -> SeatClass.ECONOMY;
            case 2 -> SeatClass.BUSINESS;
            default -> null;
        };
    }
}
