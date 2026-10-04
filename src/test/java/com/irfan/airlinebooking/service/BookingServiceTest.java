package com.irfan.airlinebooking.service;

import com.irfan.airlinebooking.enums.SeatClass;
import com.irfan.airlinebooking.exception.PaymentFailedException;
import com.irfan.airlinebooking.exception.SeatNotAvailableException;
import com.irfan.airlinebooking.model.Booking;
import com.irfan.airlinebooking.model.EconomySeat;
import com.irfan.airlinebooking.model.Flight;
import com.irfan.airlinebooking.model.Passenger;
import com.irfan.airlinebooking.model.Ticket;
import com.irfan.airlinebooking.model.UpiPayment;
import com.irfan.airlinebooking.repository.SchemaInitializer;
import com.irfan.airlinebooking.util.DbConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** End-to-end booking tests against a throwaway in-memory SQLite database. */
class BookingServiceTest {

    private static Flight flight;
    private static Passenger passenger;

    @BeforeAll
    static void setup() {
        System.setProperty("airline.dbUrl", "jdbc:sqlite::memory:");
        DbConnection.getInstance().close();
        SchemaInitializer.init();

        UserService userService = new UserService();
        passenger = userService.register("Test User", "test@airline.com", "pass123", "123");

        flight = new FlightService().addFlight(new Flight("AI-101", "Air India", "Delhi", "Mumbai",
                LocalTime.of(9, 0), LocalTime.of(11, 0),
                2.0, 0, SeatClass.ECONOMY, BigDecimal.valueOf(5000)));
    }

    @AfterAll
    static void tearDown() {
        DbConnection.getInstance().close();
        System.clearProperty("airline.dbUrl");
    }

    @BeforeEach
    void clean() {
        try (var st = DbConnection.getInstance().getConnection().createStatement()) {
            st.executeUpdate("DELETE FROM tickets");
            st.executeUpdate("DELETE FROM payments");
            st.executeUpdate("DELETE FROM bookings");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to reset tables", e);
        }
    }

    @Test
    void bookingCreatesTicketWithUniquePnr() {
        BookingService service = new BookingService();
        Ticket ticket = service.bookPassenger(flight, passenger.getId(),
                new EconomySeat("1A"), "Test User", 30,
                new UpiPayment(BigDecimal.ZERO, "user@upi"));
        assertTrue(ticket.getPnr().startsWith("PNR-"));
        assertEquals("1A", ticket.getSeatNumber());
    }

    @Test
    void doubleBookingSameSeatIsRejected() {
        BookingService service = new BookingService();
        service.bookPassenger(flight, passenger.getId(), new EconomySeat("2B"), "A", 30,
                new UpiPayment(BigDecimal.ZERO, "a@upi"));
        assertThrows(SeatNotAvailableException.class, () ->
                service.bookPassenger(flight, passenger.getId(), new EconomySeat("2B"), "B", 31,
                        new UpiPayment(BigDecimal.ZERO, "b@upi")));
    }

    @Test
    void failedPaymentFreesSeat() {
        BookingService service = new BookingService();
        assertThrows(PaymentFailedException.class, () ->
                service.bookPassenger(flight, passenger.getId(), new EconomySeat("3C"), "C", 40,
                        new UpiPayment(BigDecimal.ZERO, "invalid@upi")));
        // The seat must be available again.
        service.bookPassenger(flight, passenger.getId(), new EconomySeat("3C"), "D", 41,
                new UpiPayment(BigDecimal.ZERO, "d@upi"));
    }

    @Test
    void pnrCounterAvoidsCollisionsWithExistingRows() {
        // Simulate a fresh JVM: reset the static counter, then reseed from DB.
        Booking.seedPnrCounter(0);
        int existingHigh = new com.irfan.airlinebooking.repository.BookingRepository().highestPnrSequence();
        Booking.seedPnrCounter(existingHigh);
        Booking next = new Booking();
        String pnr1 = Booking.generatePnr();
        String pnr2 = Booking.generatePnr();
        assertTrue(Integer.parseInt(pnr1.substring(4)) > existingHigh,
                "new PNR must exceed existing sequence");
        assertTrue(Integer.parseInt(pnr2.substring(4)) > Integer.parseInt(pnr1.substring(4)));
    }

    @Test
    void cancelUpdatesStatus() {
        BookingService service = new BookingService();
        Ticket ticket = service.bookPassenger(flight, passenger.getId(),
                new EconomySeat("4D"), "E", 25, new UpiPayment(BigDecimal.ZERO, "e@upi"));
        var booking = service.cancel(ticket.getPnr(), passenger.getId(), false);
        assertEquals(com.irfan.airlinebooking.enums.BookingStatus.CANCELLED, booking.getStatus());
    }
}
