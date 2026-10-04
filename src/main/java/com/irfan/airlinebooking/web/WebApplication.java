package com.irfan.airlinebooking.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the HTML/CSS web frontend.
 * <p>
 * The console application ({@code com.irfan.airlinebooking.Main}) is kept as
 * an alternative frontend; both drive the same service layer.
 */
@SpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
