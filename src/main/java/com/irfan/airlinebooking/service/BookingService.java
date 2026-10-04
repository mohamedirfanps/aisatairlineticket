package com.irfan.airlinebooking.service;

import com.irfan.airlinebooking.enums.BookingStatus;
import com.irfan.airlinebooking.enums.PaymentStatus;
import com.irfan.airlinebooking.enums.SeatClass;
import com.irfan.airlinebooking.exception.PaymentFailedException;
import com.irfan.airlinebooking.exception.SeatNotAvailableException;
import com.irfan.airlinebooking.exception.ValidationException;
import com.irfan.airlinebooking.model.AddOns;
import com.irfan.airlinebooking.model.Booking;
import com.irfan.airlinebooking.model.Flight;
import com.irfan.airlinebooking.model.Payment;
import com.irfan.airlinebooking.model.Seat;
import com.irfan.airlinebooking.model.Ticket;
import com.irfan.airlinebooking.repository.BookingRepository;
import com.irfan.airlinebooking.repository.PaymentRepository;
import com.irfan.airlinebooking.repository.TicketRepository;
import com.irfan.airlinebooking.util.Validator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Orchestrates seat selection, payment and ticket issuance.
 * <p>
 * Seat reservation is guarded by a {@link ReentrantLock} and a database
 * re-check so that two threads cannot book the same seat (the
 * <b>multithreading</b> requirement).
 */
public class BookingService {

    /** Diagnostic counters; AtomicInteger keeps them consistent under load. */
    private static final java.util.concurrent.atomic.AtomicInteger CONFIRMED =
            new java.util.concurrent.atomic.AtomicInteger();

    private final BookingRepository bookings = new BookingRepository();
    private final TicketRepository tickets = new TicketRepository();
    private final PaymentRepository payments = new PaymentRepository();
    private final SeatService seatService = new SeatService();
    private final ReentrantLock seatLock = new ReentrantLock();
    private final java.util.concurrent.atomic.AtomicBoolean pnrSeeded =
            new java.util.concurrent.atomic.AtomicBoolean();

    /**
     * Keeps generated PNRs unique across restarts by continuing from the
     * highest sequence already stored. Runs at most once, lazily, so it is
     * safe regardless of when the schema is created.
     */
    private void ensurePnrSeeded() {
        if (pnrSeeded.compareAndSet(false, true)) {
            Booking.seedPnrCounter(bookings.highestPnrSequence());
        }
    }

    /**
     * Books a seat end-to-end: reserve, pay, persist booking + ticket.
     *
     * @param travelDate the date of travel (flights operate every day)
     * @param addOns     optional extras (luggage / meals), never null
     * @param payment    a ready-to-run payment object (polymorphic)
     * @return the created {@link Ticket}
     */
    public Ticket bookPassenger(Flight flight, Long userId, Seat seat,
                                LocalDate travelDate, AddOns addOns,
                                String passengerName, int passengerAge, Payment payment) {
        ensurePnrSeeded();
        Validator.requireText(passengerName, "Passenger name");
        if (passengerAge <= 0 || passengerAge > 120) {
            throw new ValidationException("Passenger age must be between 1 and 120");
        }
        if (flight == null || flight.getId() == null) {
            throw new ValidationException("A valid flight must be selected");
        }
        if (travelDate == null) {
            throw new ValidationException("Please choose a travel date");
        }
        if (addOns == null) {
            addOns = new AddOns();
        }
        SeatClass seatClass = seat.getSeatClass();
        if (!seatService.isValidSeat(seatClass, seat.getSeatNumber())) {
            throw new SeatNotAvailableException("Unknown seat " + seat.getSeatNumber());
        }

        seatLock.lock();
        try {
            // Re-check under the lock: prevents double booking across threads.
            if (bookings.countBookedSeat(flight.getId(), seat.getSeatNumber(), travelDate) > 0) {
                throw new SeatNotAvailableException(
                        "Seat " + seat.getSeatNumber() + " is already booked on " + travelDate);
            }

            BigDecimal seatFare = seat.calculateFare(flight.getBaseFare().doubleValue());
            BigDecimal addOnAmount = addOns.calculateAmount(seatFare);
            BigDecimal total = seatFare.add(addOnAmount);

            Booking booking = new Booking(userId, flight.getId(), passengerName.trim(), passengerAge);
            booking.setFlightNumber(flight.getFlightNumber());
            booking.setSource(flight.getSource());
            booking.setDestination(flight.getDestination());
            booking.setTravelDate(travelDate);
            booking.setSeatClass(seatClass);
            booking.setSeatNumber(seat.getSeatNumber());
            booking.setExtraLuggage(addOns.isExtraLuggage());
            booking.setMeals(addOns.isMeals());
            booking.setAddOnAmount(addOnAmount);
            booking.setFare(total);
            booking.setStatus(BookingStatus.CONFIRMED);
            bookings.save(booking);

            payment.setBookingId(booking.getId());
            payment.setAmount(total);
            PaymentStatus status = payment.processPayment();
            payments.save(payment, booking.getId());

            if (status != PaymentStatus.SUCCESS) {
                // roll the reservation back so the seat is freed
                bookings.updateStatus(booking.getId(), BookingStatus.CANCELLED);
                throw new PaymentFailedException(
                        "Payment via " + payment.describeMethod() + " failed");
            }

            CONFIRMED.incrementAndGet();
            Ticket ticket = new Ticket(booking);
            tickets.save(ticket);
            return ticket;
        } finally {
            seatLock.unlock();
        }
    }

    /** Backwards-compatible overload (no travel date / add-ons). */
    public Ticket bookPassenger(Flight flight, Long userId, Seat seat,
                                String passengerName, int passengerAge, Payment payment) {
        return bookPassenger(flight, userId, seat, LocalDate.now().plusDays(1), new AddOns(),
                passengerName, passengerAge, payment);
    }

    /** Cancels a booking (only its owner or an admin should call this). */
    public Booking cancel(String pnr, Long requestingUserId, boolean isAdmin) {
        Booking booking = bookings.findByPnr(pnr)
                .orElseThrow(() -> new ValidationException("No booking with PNR " + pnr));
        if (!isAdmin && !booking.getUserId().equals(requestingUserId)) {
            throw new ValidationException("You can only cancel your own bookings");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ValidationException("Booking " + pnr + " is already cancelled");
        }
        bookings.updateStatus(booking.getId(), BookingStatus.CANCELLED);
        booking.setStatus(BookingStatus.CANCELLED);
        return booking;
    }

    /** True if the seat is already taken on the flight for the given date. */
    public boolean isSeatTaken(Long flightId, String seatNumber, LocalDate travelDate) {
        return bookings.countBookedSeat(flightId, seatNumber, travelDate) > 0;
    }

    /** True if the seat is booked on any date (used by the simple overlays). */
    public boolean isSeatTaken(Long flightId, String seatNumber) {
        return bookings.countBookedSeat(flightId, seatNumber) > 0;
    }

    /** Admin: update the editable fields of a booking (PNR is immutable). */
    public void updateBooking(Booking booking) {
        bookings.updateEditableFields(booking);
    }

    public Booking findByPnr(String pnr) {
        return bookings.findByPnr(pnr)
                .orElseThrow(() -> new ValidationException("No booking with PNR " + pnr));
    }

    public Booking findById(Long id) {
        return bookings.findById(id)
                .orElseThrow(() -> new ValidationException("No booking with id " + id));
    }

    public List<Booking> bookingsForUser(Long userId) {
        return bookings.findByUser(userId);
    }

    public List<Booking> allBookings() {
        return bookings.findAll();
    }

    public Ticket ticketForPnr(String pnr) {
        return tickets.findByPnr(pnr)
                .orElseThrow(() -> new ValidationException("No ticket for PNR " + pnr));
    }

    public Payment paymentForBooking(Long bookingId) {
        return payments.findByBooking(bookingId).orElse(null);
    }

    public static int confirmedCount() {
        return CONFIRMED.get();
    }

    /** Convenience for tests: start of the current day. */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}
