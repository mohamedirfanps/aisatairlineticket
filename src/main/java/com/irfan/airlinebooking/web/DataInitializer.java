package com.irfan.airlinebooking.web;

import com.irfan.airlinebooking.repository.FlightNetworkGenerator;
import com.irfan.airlinebooking.repository.SchemaInitializer;
import com.irfan.airlinebooking.service.FlightService;
import com.irfan.airlinebooking.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Prepares the database on startup: create schema, seed default accounts, and
 * generate the flight network once (mirrors what {@code Main} does for console).
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserService userService;
    private final FlightService flightService;

    public DataInitializer(UserService userService, FlightService flightService) {
        this.userService = userService;
        this.flightService = flightService;
    }

    @Override
    public void run(ApplicationArguments args) {
        SchemaInitializer.init();
        userService.seedDefaults();
        if (flightService.count() == 0) {
            try {
                int inserted = flightService.seedNetwork();
                log.info("Generated {} flights across the network", inserted);
            } catch (RuntimeException e) {
                log.warn("Flight network generation skipped: {}", e.getMessage());
            }
        }
        log.info("Startup complete. {} flights available.", flightService.count());
    }
}
