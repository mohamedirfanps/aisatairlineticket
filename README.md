# Airline Booking System

An airline ticket booking project in Java 21 with **two frontends**:

- **Web (primary)** — HTML/CSS pages served by Spring Boot + Thymeleaf.
- **Console (alternative)** — the original terminal menus.

Both drive the same framework-free `service` / `repository` / `model` layers.
The flight network (150+ airports across India and the world) is **generated
once** on first run with distance-based, static fares (this is a demo, not a
live booking site).

## Tech stack

| Layer      | Choice                                        |
|------------|-----------------------------------------------|
| Language   | Java 21 (builds on JDK 21+)                   |
| Web        | Spring Boot 3.5 + Spring MVC + Thymeleaf      |
| Frontend   | HTML5 + CSS (templates in `src/main/resources`)|
| Build      | Maven                                         |
| Database   | SQLite via JDBC (`org.xerial:sqlite-jdbc`)    |
| Tests      | JUnit 5 (+ Spring MVC `MockMvc`)              |
| Data       | Programmatically generated flight network     |

## Prerequisites

- JDK 21 or newer (`java -version`)
- Maven (or IntelliJ's bundled Maven)
- No database server and no API keys required.

## Running the web app

```bash
mvn spring-boot:run
```

Then open <http://localhost:8080>.

On first run the app creates `data/airline.db`, seeds two accounts, and
generates ~1,700 flights across the airport network.

Build a runnable web jar:

```bash
mvn package
java -jar target/airline-booking-system-1.0-SNAPSHOT.jar
```

Run on a different port if 8080 is taken:

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
```

## Running the console version

```bash
mvn compile exec:java
```

## Default logins

| Role      | Email               | Password |
|-----------|---------------------|----------|
| Admin     | admin@airline.com   | admin123 |
| Passenger | demo@airline.com    | demo123  |

## Booking flow

1. **Search** — pick From/To city, **travel date**, class and sort order.
2. **Add-ons** — optionally choose **extra luggage (+10% of fare)** and/or
   **meals (+₹500)**; the cost is folded into the price shown next.
3. **Results** — flights with Fare, Add-ons and **Total** columns. Flights
   operate every day, so no date is shown in the list. Results support
   **airline + min/max price filters** and **pagination** (10/20/50/100 per
   page).
4. **Book** — choose a seat, enter passenger details, see the fare summary
   (seat + add-ons), pay, and get an e-ticket (PNR).

Seat availability is per flight **and per travel date**, so the same seat can
be booked on different days.

## Web pages

| URL                     | Who       | Purpose                                   |
|-------------------------|-----------|-------------------------------------------|
| `/login`, `/register`   | public    | sign in / create account                  |
| `/search`               | passenger | search criteria (route, date, class)      |
| `/addons`               | passenger | choose luggage / meals                    |
| `/results`              | passenger | matching flights with totals              |
| `/book/{id}`            | passenger | seat map + passenger details + payment    |
| `/bookings`             | passenger | my reservations, ticket view, cancel      |
| `/ticket?pnr=…`         | passenger | e-ticket (printable)                      |
| `/admin`                | admin     | dashboard linking to the manage pages     |
| `/admin/flights`        | admin     | list, filter, add, edit, delete, view     |
| `/admin/passengers`     | admin     | list and edit passenger details           |
| `/admin/bookings`       | admin     | list and edit bookings (PNR immutable)    |
| `/admin/reports`        | admin     | revenue, route stats, all bookings        |

Only admin routes can open the manage pages; all pages except login/register
require a session.

Payment simulation rules (deterministic, for reproducible demos):

- UPI: succeeds when the id contains `@`; fails if it contains `invalid`.
- Card: fails when the number ends in `0000`.
- Net banking: succeeds when a bank name is given.

## Configuration

Override defaults with JVM system properties:

| Property             | Default                       | Meaning                       |
|----------------------|-------------------------------|-------------------------------|
| `airline.dbUrl`      | `jdbc:sqlite:data/airline.db` | JDBC URL                      |

Web settings live in `src/main/resources/application.properties`
(port, Thymeleaf cache). To regenerate flights, delete `data/airline.db`.

## Project layout

```
src/main/java/com/irfan/airlinebooking/
├── model/       User/Admin/Passenger, Flight, Airport, Seat(+Economy/Business),
│                Booking, Ticket, Payment(+Card/Upi/NetBanking), AddOns, Schedule
├── enums/       SeatClass, Role, BookingStatus, PaymentStatus
├── repository/  Repository<T,ID> + JDBC DAOs, SchemaInitializer,
│                FlightNetworkGenerator
├── service/     UserService, FlightService, SeatService, BookingService, ReportService
├── ui/          Console, LoginScreen, PassengerMenu, AdminMenu   (console frontend)
├── web/         WebApplication, controllers, ServiceConfig, SessionUser, forms
├── util/        Config, DbConnection (singleton), Airports (catalog), Validator
├── exception/   custom runtime exceptions
└── Main.java    console entry point;  web/WebApplication.java = web entry point

src/main/resources/
├── templates/   Thymeleaf pages (login, search, addons, results, book, ticket,
│                bookings, admin/*)
├── static/css/  app.css
└── application.properties
```

## OOP / syllabus mapping

- **Encapsulation** — private fields + accessors throughout `model`.
- **Abstraction** — `User`, `Seat`, `Payment` are abstract with abstract methods.
- **Inheritance** — `Admin`/`Passenger` extend `User`; `EconomySeat`/`BusinessSeat`
  extend `Seat`; payment subclasses extend `Payment`.
- **Overloading** — `BookingService.bookPassenger` overloads; seat constructors.
- **Overriding (runtime polymorphism)** — `Seat.calculateFare`, `Payment.processPayment`.
- **Generics** — `Repository<T, ID>`.
- **Collections** — `List`, `Map` (route grouping), stream collectors.
- **Streams & lambdas** — flight sorting, report aggregation.
- **Multithreading** — `ReentrantLock` in `BookingService` guarantees a seat
  cannot be double-booked (see `ConcurrentBookingTest`).
- **Custom exceptions** — `SeatNotAvailableException`, `PaymentFailedException`, etc.
- **Design patterns** — Singleton (`DbConnection`), DAO (`repository`), MVC
  (`web` controllers / `service` / `templates`).

## Notes

- The flight network is generated by `FlightNetworkGenerator` from the
  `Airports` catalog (all major Indian airports plus worldwide hubs).
  Fares are distance-based and frozen at generation time.
- `data/*.csv` (the original Kaggle files) are kept for reference but are no
  longer used.

