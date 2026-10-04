package com.irfan.airlinebooking.service;

import com.irfan.airlinebooking.enums.SeatClass;
import com.irfan.airlinebooking.model.EconomySeat;
import com.irfan.airlinebooking.model.Flight;
import com.irfan.airlinebooking.model.Passenger;
import com.irfan.airlinebooking.model.UpiPayment;
import com.irfan.airlinebooking.repository.SchemaInitializer;
import com.irfan.airlinebooking.util.DbConnection;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that concurrent booking attempts for the same seat result in
 * exactly one success (the synchronized / locked reservation requirement).
 */
class ConcurrentBookingTest {

    @BeforeAll
    static void setup() {
        System.setProperty("airline.dbUrl", "jdbc:sqlite::memory:");
        DbConnection.getInstance().close();
        SchemaInitializer.init();
    }

    @Test
    void onlyOneThreadBooksTheSameSeat() throws InterruptedException {
        Passenger passenger = new UserService()
                .register("Race", "race@airline.com", "pw", "1");
        Flight flight = new FlightService().addFlight(new Flight("RACE-1", "Test Air", "Delhi", "Chennai",
                LocalTime.of(9, 0), LocalTime.of(11, 0),
                2.0, 0, SeatClass.ECONOMY, BigDecimal.valueOf(5000)));

        BookingService service = new BookingService();
        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            final int n = i;
            pool.submit(() -> {
                try {
                    start.await();
                    service.bookPassenger(flight, passenger.getId(), new EconomySeat("1A"),
                            "P" + n, 20 + n, new UpiPayment(BigDecimal.ZERO, "p" + n + "@upi"));
                    success.incrementAndGet();
                } catch (Exception ignored) {
                    // expected for all but one thread
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        pool.shutdown();

        assertEquals(1, success.get(), "Exactly one booking should succeed for seat 1A");
    }
}
