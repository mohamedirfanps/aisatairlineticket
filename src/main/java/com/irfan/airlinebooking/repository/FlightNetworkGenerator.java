package com.irfan.airlinebooking.repository;

import com.irfan.airlinebooking.enums.SeatClass;
import com.irfan.airlinebooking.model.Airport;
import com.irfan.airlinebooking.model.Flight;
import com.irfan.airlinebooking.util.Airports;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates a realistic flight network across every airport in
 * {@link Airports}.
 * <p>
 * The original Kaggle CSV only covered seven Indian cities, which is why the
 * airport list was so limited. This generator:
 * <ul>
 *   <li>connects Indian airports to each other (domestic network),</li>
 *   <li>connects Indian airports to a set of international hubs,</li>
 * </ul>
 * assigning each route an airline, flight number, duration (from great-circle
 * distance) and distance-based economy fare. Each route also gets a Business
 * variant. Fares are static and deterministic (seeded RNG) so demos repeat.
 */
public class FlightNetworkGenerator {

    private final Random random = new Random(20261004L);

    /** Indian airlines weighted by relative size. */
    private static final String[] DOMESTIC_AIRLINES = {
            "Indigo", "Air India", "Vistara", "SpiceJet", "AkasaAir",
            "AirAsia", "GO FIRST", "AllianceAir", "StarAir"
    };

    /** Airlines used for international routes (mix of Indian and foreign). */
    private static final String[] INTERNATIONAL_AIRLINES = {
            "Air India", "Indigo", "Emirates", "Qatar Airways", "Etihad",
            "Singapore Airlines", "British Airways", "Lufthansa", "Thai Airways",
            "Air France", "KLM", "Turkish Airlines"
    };

    /** Hubs that every Indian metro connects to. */
    private static final String[] INTERNATIONAL_HUBS = {
            "DXB", "AUH", "DOH", "SIN", "BKK", "KUL", "LHR", "CDG", "FRA",
            "AMS", "HKG", "NRT", "ICN", "JFK", "SYD", "IST", "DPS", "MNL",
            "CMB", "KTM", "DAC", "MLE", "NBO", "MRU", "YYZ", "LAX"
    };

    /** Larger Indian airports that act as domestic hubs. */
    private static final String[] INDIAN_HUBS = {
            "DEL", "BOM", "BLR", "MAA", "CCU", "HYD", "AMD", "COK", "GOI", "PNQ"
    };

    private int flightSequence = 100;

    /** Builds the full list of flights (not yet persisted). */
    public List<Flight> generate() {
        List<Flight> flights = new ArrayList<>();

        List<Airport> india = Airports.byCountry("India");

        // 1) Domestic: every Indian hub <-> every other Indian airport.
        for (String hubCode : INDIAN_HUBS) {
            Airport hub = Airports.byCode(hubCode);
            for (Airport other : india) {
                if (other.getCode().equals(hub.getCode())) {
                    continue;
                }
                addRoute(flights, hub, other, false);
            }
        }

        // 2) International: Indian hubs <-> international hubs.
        for (String hubCode : INDIAN_HUBS) {
            Airport indianHub = Airports.byCode(hubCode);
            for (String intlCode : INTERNATIONAL_HUBS) {
                Airport intl = Airports.byCode(intlCode);
                addRoute(flights, indianHub, intl, true);
            }
        }

        // 3) A few international-to-international trunk routes.
        String[][] trunk = {
                {"DXB", "LHR"}, {"DXB", "JFK"}, {"SIN", "SYD"}, {"SIN", "NRT"},
                {"HKG", "LAX"}, {"DOH", "CDG"}, {"IST", "JFK"}, {"LHR", "JFK"},
                {"BKK", "SYD"}, {"KUL", "MEL"}
        };
        for (String[] pair : trunk) {
            addRoute(flights, Airports.byCode(pair[0]), Airports.byCode(pair[1]), true);
        }

        return flights;
    }

    private void addRoute(List<Flight> flights, Airport from, Airport to, boolean international) {
        double distance = Airports.distanceKm(from.getCode(), to.getCode());
        if (distance < 150) {
            return; // too close to be a route
        }
        // Duration from distance: ~750 km/h plus taxi/climb overhead.
        double duration = roundToQuarter(distance / 750.0 + 0.4);

        String airline = pick(international ? INTERNATIONAL_AIRLINES : DOMESTIC_AIRLINES);
        String code = airlineCode(airline);

        // One Economy and one Business flight per route, at different times.
        EconomyFare fare = economyFare(distance, international);

        flights.add(build(from, to, airline, code, duration, fare,
                randomDeparture(), international, SeatClass.ECONOMY, fare.economy));
        flights.add(build(from, to, airline, code, duration, fare,
                randomDeparture(), international, SeatClass.BUSINESS, fare.economy));
    }

    /**
     * Distance-based economy fare.
     * <p>
     * Formula: a fixed component plus a per-km component, with a higher rate
     * for international travel. Returns both the economy fare and the
     * economy-equivalent base (which is simply the economy fare, since
     * Business applies its own multiplier downstream).
     */
    private EconomyFare economyFare(double distanceKm, boolean international) {
        double perKm = international ? 5.5 : 4.0;
        double base = international ? 6000 : 1200;
        double amount = base + perKm * distanceKm;
        // Add a little seeded variation so routes aren't perfectly linear.
        amount *= 0.92 + random.nextDouble() * 0.16;
        BigDecimal economy = BigDecimal.valueOf(amount).setScale(0, RoundingMode.HALF_UP);
        return new EconomyFare(economy);
    }

    private Flight build(Airport from, Airport to, String airline, String code,
                         double duration, EconomyFare fare, LocalTime departure,
                         boolean international, SeatClass seatClass, BigDecimal base) {
        LocalTime arrival = departure.plusMinutes(Math.round(duration * 60));
        String number = code + "-" + (++flightSequence);
        return new Flight(number, airline, from.getCity(), to.getCity(),
                departure, arrival, duration, 0, seatClass, base);
    }

    private LocalTime randomDeparture() {
        // Departures on the quarter hour between 05:00 and 22:00.
        int hour = 5 + random.nextInt(17);
        int minute = random.nextInt(4) * 15;
        return LocalTime.of(hour, minute);
    }

    private double roundToQuarter(double hours) {
        return Math.round(hours * 4.0) / 4.0;
    }

    private String pick(String[] values) {
        return values[random.nextInt(values.length)];
    }

    /** Two-letter airline code derived from the airline name. */
    private String airlineCode(String airline) {
        return switch (airline) {
            case "Indigo" -> "6E";
            case "Air India" -> "AI";
            case "Vistara" -> "UK";
            case "SpiceJet" -> "SG";
            case "AkasaAir" -> "QP";
            case "AirAsia" -> "AK";
            case "GO FIRST" -> "G8";
            case "AllianceAir" -> "9I";
            case "StarAir" -> "S5";
            case "Emirates" -> "EK";
            case "Qatar Airways" -> "QR";
            case "Etihad" -> "EY";
            case "Singapore Airlines" -> "SQ";
            case "British Airways" -> "BA";
            case "Lufthansa" -> "LH";
            case "Thai Airways" -> "TG";
            case "Air France" -> "AF";
            case "KLM" -> "KL";
            case "Turkish Airlines" -> "TK";
            default -> "XX";
        };
    }

    /** Holds the computed economy fare for a route. */
    private record EconomyFare(BigDecimal economy) {
    }
}
