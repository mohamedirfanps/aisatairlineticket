package com.irfan.airlinebooking.web;

import com.irfan.airlinebooking.exception.ValidationException;
import com.irfan.airlinebooking.model.AddOns;
import com.irfan.airlinebooking.model.CardPayment;
import com.irfan.airlinebooking.model.Flight;
import com.irfan.airlinebooking.model.NetBankingPayment;
import com.irfan.airlinebooking.model.Payment;
import com.irfan.airlinebooking.model.Seat;
import com.irfan.airlinebooking.model.Ticket;
import com.irfan.airlinebooking.model.UpiPayment;
import com.irfan.airlinebooking.model.User;
import com.irfan.airlinebooking.service.BookingService;
import com.irfan.airlinebooking.service.FlightService;
import com.irfan.airlinebooking.service.SeatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Seat selection, payment, ticket display, booking history and cancellation. */
@Controller
public class BookingController {

    private final FlightService flightService;
    private final SeatService seatService;
    private final BookingService bookingService;

    public BookingController(FlightService flightService, SeatService seatService,
                             BookingService bookingService) {
        this.flightService = flightService;
        this.seatService = seatService;
        this.bookingService = bookingService;
    }

    /** Seat + passenger + payment form for a flight. */
    @GetMapping("/book/{flightId}")
    public String showBookingForm(@PathVariable Long flightId,
                                  @RequestParam(required = false) String travelDate,
                                  @RequestParam(required = false, defaultValue = "false") boolean luggage,
                                  @RequestParam(required = false, defaultValue = "false") boolean meals,
                                  Model model) {
        Flight flight = flightService.findById(flightId);
        LocalDate date = parseDate(travelDate);
        List<Seat> seats = seatService.seatMap(flight.getSeatClass());
        java.util.Set<String> booked = seats.stream()
                .map(Seat::getSeatNumber)
                .filter(s -> bookingService.isSeatTaken(flightId, s, date))
                .collect(java.util.stream.Collectors.toSet());
        AddOns addOns = new AddOns(luggage, meals);
        BigDecimal classFare = flight.getFare();

        model.addAttribute("flight", flight);
        model.addAttribute("seats", seats);
        model.addAttribute("bookedSeats", booked);
        model.addAttribute("travelDate", date);
        model.addAttribute("addOns", addOns);
        model.addAttribute("seatFare", classFare);
        model.addAttribute("addOnAmount", addOns.calculateAmount(classFare));

        BookingForm bookingForm = new BookingForm();
        bookingForm.setFlightId(flightId);
        model.addAttribute("bookingForm", bookingForm);
        return "book";
    }

    /** Process the booking and show the ticket. */
    @PostMapping("/book")
    public String submit(@ModelAttribute BookingForm form,
                         HttpServletRequest request,
                         Model model) {
        User user = SessionUser.get(request);
        if (form.getFlightId() == null) {
            throw new ValidationException("Please select a flight before booking");
        }
        Flight flight = flightService.findById(form.getFlightId());
        LocalDate travelDate = parseDate(form.getTravelDate() == null
                ? null : form.getTravelDate().toString());

        Seat seat = seatService.seatMap(flight.getSeatClass()).stream()
                .filter(s -> s.getSeatNumber().equalsIgnoreCase(
                        form.getSeatNumber() == null ? "" : form.getSeatNumber().trim()))
                .findFirst()
                .orElseThrow(() -> new ValidationException("Unknown seat " + form.getSeatNumber()));

        AddOns addOns = new AddOns(form.isExtraLuggage(), form.isMeals());
        BigDecimal seatFare = seat.calculateFare(flight.getBaseFare().doubleValue());
        BigDecimal total = seatFare.add(addOns.calculateAmount(seatFare));

        Payment payment = buildPayment(form, total);
        if (payment == null) {
            throw new ValidationException("Please choose a payment method");
        }

        Ticket ticket = bookingService.bookPassenger(flight, user.getId(), seat,
                travelDate, addOns, form.getPassengerName(),
                form.getPassengerAge() == null ? 0 : form.getPassengerAge(), payment);

        model.addAttribute("ticket", ticket);
        model.addAttribute("flight", flight);
        return "ticket";
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return LocalDate.now().plusDays(1);
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            throw new ValidationException("Invalid travel date: " + value);
        }
    }

    /** The passenger's own bookings. */
    @GetMapping("/bookings")
    public String myBookings(HttpServletRequest request, Model model) {
        User user = SessionUser.get(request);
        model.addAttribute("bookings", bookingService.bookingsForUser(user.getId()));
        return "bookings";
    }

    @PostMapping("/bookings/cancel")
    public String cancel(@RequestParam String pnr, HttpServletRequest request, Model model) {
        User user = SessionUser.get(request);
        bookingService.cancel(pnr, user.getId(), user instanceof com.irfan.airlinebooking.model.Admin);
        return "redirect:/bookings";
    }

    /** Ticket lookup by PNR. */
    @GetMapping("/ticket")
    public String ticket(@RequestParam String pnr, Model model) {
        model.addAttribute("ticket", bookingService.ticketForPnr(pnr));
        return "ticket";
    }

    private Payment buildPayment(BookingForm form, BigDecimal fare) {
        String method = form.getPaymentMethod() == null ? "" : form.getPaymentMethod().toUpperCase();
        return switch (method) {
            case "UPI" -> new UpiPayment(fare, form.getUpiId());
            case "CARD" -> new CardPayment(fare, form.getCardNumber(), form.getCardHolder());
            case "NETBANKING" -> new NetBankingPayment(fare, form.getBank());
            default -> null;
        };
    }
}
