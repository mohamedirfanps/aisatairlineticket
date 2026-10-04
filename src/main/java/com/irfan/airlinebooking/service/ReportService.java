package com.irfan.airlinebooking.service;

import com.irfan.airlinebooking.enums.BookingStatus;
import com.irfan.airlinebooking.model.Booking;
import com.irfan.airlinebooking.repository.BookingRepository;

import java.math.BigDecimal;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin reporting. Uses the <b>Streams API</b> for aggregation
 * (sum, count, grouping).
 */
public class ReportService {

    private final BookingRepository bookings = new BookingRepository();

    public List<Booking> allBookings() {
        return bookings.findAll();
    }

    /** Total revenue from confirmed bookings. */
    public BigDecimal totalRevenue() {
        return bookings.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getFare)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long confirmedCount() {
        return bookings.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
                .count();
    }

    public long cancelledCount() {
        return bookings.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.CANCELLED)
                .count();
    }

    /** Number of bookings per route, most popular first. */
    public Map<String, Long> bookingsByRoute() {
        return bookings.findAll().stream()
                .collect(Collectors.groupingBy(
                        b -> b.getSource() + " -> " + b.getDestination(),
                        Collectors.counting()));
    }

    /** Descriptive fare statistics across all bookings. */
    public DoubleSummaryStatistics fareStatistics() {
        return bookings.findAll().stream()
                .filter(b -> b.getFare() != null)
                .mapToDouble(b -> b.getFare().doubleValue())
                .summaryStatistics();
    }
}
