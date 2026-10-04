package com.irfan.airlinebooking.web;

import com.irfan.airlinebooking.service.BookingService;
import com.irfan.airlinebooking.service.FlightService;
import com.irfan.airlinebooking.service.ReportService;
import com.irfan.airlinebooking.service.SeatService;
import com.irfan.airlinebooking.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exposes the framework-free service layer as Spring beans.
 * <p>
 * Kept separate from {@code @Service} annotations so the service/repository
 * packages stay independent of Spring (and stay reusable by the console app).
 */
@Configuration
public class ServiceConfig {

    @Bean
    public UserService userService() {
        return new UserService();
    }

    @Bean
    public FlightService flightService() {
        return new FlightService();
    }

    @Bean
    public SeatService seatService() {
        return new SeatService();
    }

    @Bean
    public BookingService bookingService() {
        return new BookingService();
    }

    @Bean
    public ReportService reportService() {
        return new ReportService();
    }
}
