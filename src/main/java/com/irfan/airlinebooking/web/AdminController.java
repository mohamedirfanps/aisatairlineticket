package com.irfan.airlinebooking.web;

import com.irfan.airlinebooking.enums.BookingStatus;
import com.irfan.airlinebooking.enums.SeatClass;
import com.irfan.airlinebooking.exception.ValidationException;
import com.irfan.airlinebooking.model.Admin;
import com.irfan.airlinebooking.model.Booking;
import com.irfan.airlinebooking.model.Flight;
import com.irfan.airlinebooking.model.Passenger;
import com.irfan.airlinebooking.model.User;
import com.irfan.airlinebooking.service.BookingService;
import com.irfan.airlinebooking.service.FlightService;
import com.irfan.airlinebooking.service.ReportService;
import com.irfan.airlinebooking.service.UserService;
import com.irfan.airlinebooking.util.Airports;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Admin area: a dashboard linking to flight management, passenger management
 * and booking management (all editable), plus reports. Admin-only.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final FlightService flightService;
    private final ReportService reportService;
    private final UserService userService;
    private final BookingService bookingService;

    public AdminController(FlightService flightService, ReportService reportService,
                           UserService userService, BookingService bookingService) {
        this.flightService = flightService;
        this.reportService = reportService;
        this.userService = userService;
        this.bookingService = bookingService;
    }

    private void requireAdmin(HttpServletRequest request) {
        if (!SessionUser.isAdmin(request)) {
            throw new ValidationException("Admin access required");
        }
    }

    /* ---------------- Dashboard ---------------- */

    @GetMapping({"", "/"})
    public String dashboard(HttpServletRequest request, Model model) {
        requireAdmin(request);
        model.addAttribute("flightCount", flightService.count());
        model.addAttribute("passengerCount", userService.passengers().size());
        model.addAttribute("bookingCount", bookingService.allBookings().size());
        model.addAttribute("revenue", reportService.totalRevenue());
        return "admin/dashboard";
    }

    /* ---------------- Flights ---------------- */

    @GetMapping("/flights")
    public String flightsList(@RequestParam(required = false) String q,
                              HttpServletRequest request, Model model) {
        requireAdmin(request);
        List<Flight> flights = flightService.search(q, null, null);
        model.addAttribute("flights", flights);
        model.addAttribute("total", flights.size());
        model.addAttribute("q", q);
        return "admin/flights";
    }

    @GetMapping("/flights/{id}")
    public String flightDetail(@org.springframework.web.bind.annotation.PathVariable Long id,
                               HttpServletRequest request, Model model) {
        requireAdmin(request);
        model.addAttribute("flight", flightService.findById(id));
        return "admin/flight-detail";
    }

    @GetMapping("/flights/new")
    public String newFlightForm(HttpServletRequest request, Model model) {
        requireAdmin(request);
        model.addAttribute("flightForm", blankForm());
        model.addAttribute("airports", Airports.all());
        model.addAttribute("edit", false);
        return "admin/flight-form";
    }

    @GetMapping("/flights/{id}/edit")
    public String editFlightForm(@org.springframework.web.bind.annotation.PathVariable Long id,
                                 HttpServletRequest request, Model model) {
        requireAdmin(request);
        Flight f = flightService.findById(id);
        FlightForm form = new FlightForm();
        form.setId(f.getId());
        form.setFlightNumber(f.getFlightNumber());
        form.setAirline(f.getAirline());
        form.setSource(f.getSource());
        form.setDestination(f.getDestination());
        form.setDurationHours(f.getDurationHours());
        form.setStops(f.getStops());
        form.setSeatClass(f.getSeatClass());
        form.setBaseFare(f.getBaseFare());
        model.addAttribute("flightForm", form);
        model.addAttribute("airports", Airports.all());
        model.addAttribute("edit", true);
        return "admin/flight-form";
    }

    @PostMapping("/flights/save")
    public String saveFlight(@ModelAttribute FlightForm form,
                             HttpServletRequest request,
                             RedirectAttributes redirect) {
        requireAdmin(request);
        Flight flight = form.getId() == null ? blankFlight() : flightService.findById(form.getId());
        flight.setFlightNumber(form.getFlightNumber());
        flight.setAirline(form.getAirline());
        flight.setSource(form.getSource());
        flight.setDestination(form.getDestination());
        flight.setDurationHours(form.getDurationHours());
        flight.setStops(form.getStops());
        flight.setSeatClass(form.getSeatClass());
        flight.setBaseFare(form.getBaseFare() == null ? BigDecimal.ZERO : form.getBaseFare());

        if (flight.getDepartureTime() == null) {
            LocalTime departure = LocalTime.of(9, 0);
            flight.setDepartureTime(departure);
            flight.setArrivalTime(departure.plusMinutes(Math.round(form.getDurationHours() * 60)));
        }

        if (form.getId() == null) {
            flightService.addFlight(flight);
            redirect.addFlashAttribute("message", "Flight added.");
        } else {
            flightService.updateFlight(flight);
            redirect.addFlashAttribute("message", "Flight updated.");
        }
        return "redirect:/admin/flights";
    }

    @PostMapping("/flights/delete")
    public String deleteFlight(@RequestParam Long id, HttpServletRequest request,
                               RedirectAttributes redirect) {
        requireAdmin(request);
        boolean deleted = flightService.deleteFlight(id);
        redirect.addFlashAttribute("message", deleted ? "Flight deleted." : "Flight not found.");
        return "redirect:/admin/flights";
    }

    /* ---------------- Passengers ---------------- */

    @GetMapping("/passengers")
    public String passengers(HttpServletRequest request, Model model) {
        requireAdmin(request);
        model.addAttribute("passengers", userService.passengers());
        return "admin/passengers";
    }

    @GetMapping("/passengers/{id}/edit")
    public String editPassengerForm(@org.springframework.web.bind.annotation.PathVariable Long id,
                                    HttpServletRequest request, Model model) {
        requireAdmin(request);
        User user = userService.findById(id);
        if (!(user instanceof Passenger)) {
            throw new ValidationException("Only passenger accounts can be edited here");
        }
        model.addAttribute("passengerForm", toForm((Passenger) user));
        return "admin/passenger-form";
    }

    @PostMapping("/passengers/save")
    public String savePassenger(@ModelAttribute PassengerForm form,
                                HttpServletRequest request,
                                RedirectAttributes redirect) {
        requireAdmin(request);
        userService.updatePassenger(form.getId(), form.getName(), form.getEmail(),
                form.getPhone(), form.getPassword());
        redirect.addFlashAttribute("message", "Passenger updated.");
        return "redirect:/admin/passengers";
    }

    /* ---------------- Bookings ---------------- */

    @GetMapping("/bookings")
    public String bookings(HttpServletRequest request, Model model) {
        requireAdmin(request);
        model.addAttribute("bookings", bookingService.allBookings());
        return "admin/bookings";
    }

    @GetMapping("/bookings/{id}/edit")
    public String editBookingForm(@org.springframework.web.bind.annotation.PathVariable Long id,
                                  HttpServletRequest request, Model model) {
        requireAdmin(request);
        Booking b = bookingService.findById(id);
        AdminBookingForm form = new AdminBookingForm();
        form.setId(b.getId());
        form.setPnr(b.getPnr());
        form.setPassengerName(b.getPassengerName());
        form.setPassengerAge(b.getPassengerAge());
        form.setTravelDate(b.getTravelDate() == null ? null : b.getTravelDate().toString());
        form.setSeatClass(b.getSeatClass());
        form.setSeatNumber(b.getSeatNumber());
        form.setExtraLuggage(b.isExtraLuggage());
        form.setMeals(b.isMeals());
        form.setFare(b.getFare());
        form.setStatus(b.getStatus());
        model.addAttribute("booking", b);
        model.addAttribute("bookingForm", form);
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("seatClasses", SeatClass.values());
        return "admin/booking-form";
    }

    @PostMapping("/bookings/save")
    public String saveBooking(@ModelAttribute AdminBookingForm form,
                              HttpServletRequest request,
                              RedirectAttributes redirect) {
        requireAdmin(request);
        Booking b = bookingService.findById(form.getId());
        // PNR is immutable: it is never read from the form.
        b.setPassengerName(form.getPassengerName());
        b.setPassengerAge(form.getPassengerAge() == null ? 0 : form.getPassengerAge());
        b.setTravelDate(form.getTravelDate() == null || form.getTravelDate().isBlank()
                ? null : LocalDate.parse(form.getTravelDate()));
        b.setSeatClass(form.getSeatClass());
        b.setSeatNumber(form.getSeatNumber());
        b.setExtraLuggage(form.isExtraLuggage());
        b.setMeals(form.isMeals());
        b.setStatus(form.getStatus());
        bookingService.updateBooking(b);
        redirect.addFlashAttribute("message", "Booking " + b.getPnr() + " updated.");
        return "redirect:/admin/bookings";
    }

    /* ---------------- Reports ---------------- */

    @GetMapping("/reports")
    public String reports(HttpServletRequest request, Model model) {
        requireAdmin(request);
        var stats = reportService.fareStatistics();
        model.addAttribute("revenue", reportService.totalRevenue());
        model.addAttribute("confirmed", reportService.confirmedCount());
        model.addAttribute("cancelled", reportService.cancelledCount());
        model.addAttribute("byRoute", reportService.bookingsByRoute());
        model.addAttribute("avgFare", stats.getCount() == 0 ? 0 : stats.getAverage());
        model.addAttribute("maxFare", stats.getCount() == 0 ? 0 : stats.getMax());
        model.addAttribute("minFare", stats.getCount() == 0 ? 0 : stats.getMin());
        model.addAttribute("bookings", reportService.allBookings());
        return "admin/reports";
    }

    /* ---------------- Helpers ---------------- */

    private Flight blankFlight() {
        return new Flight("", "", "", "", LocalTime.of(9, 0), LocalTime.of(11, 0),
                2.0, 0, SeatClass.ECONOMY, BigDecimal.ZERO);
    }

    private FlightForm blankForm() {
        FlightForm form = new FlightForm();
        form.setFlightNumber("");
        form.setAirline("");
        form.setSource("");
        form.setDestination("");
        form.setDurationHours(2.0);
        form.setStops(0);
        form.setSeatClass(SeatClass.ECONOMY);
        form.setBaseFare(BigDecimal.valueOf(5000));
        return form;
    }

    private PassengerForm toForm(Passenger p) {
        PassengerForm form = new PassengerForm();
        form.setId(p.getId());
        form.setName(p.getName());
        form.setEmail(p.getEmail());
        form.setPhone(p.getPhone());
        return form;
    }
}
