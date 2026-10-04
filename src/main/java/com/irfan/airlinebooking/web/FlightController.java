package com.irfan.airlinebooking.web;

import com.irfan.airlinebooking.enums.SeatClass;
import com.irfan.airlinebooking.model.AddOns;
import com.irfan.airlinebooking.model.Flight;
import com.irfan.airlinebooking.service.FlightService;
import com.irfan.airlinebooking.util.Airports;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Flight search flow:
 * <ol>
 *   <li>{@code GET /search} — search criteria (route, class, date)</li>
 *   <li>{@code GET /addons} — choose optional extras (luggage / meals)</li>
 *   <li>{@code GET /results} — matching flights with total prices</li>
 * </ol>
 */
@Controller
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    /** Step 1: search criteria. */
    @GetMapping("/search")
    public String searchForm(Model model) {
        model.addAttribute("airports", Airports.all());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("defaultDate", LocalDate.now().plusDays(1));
        return "search";
    }

    /** Step 2: add-ons page. */
    @GetMapping("/addons")
    public String addOns(@RequestParam(required = false) String source,
                         @RequestParam(required = false) String destination,
                         @RequestParam(required = false) String seatClass,
                         @RequestParam(required = false) String travelDate,
                         @RequestParam(required = false, defaultValue = "fare") String sort,
                         Model model) {
        model.addAttribute("source", source);
        model.addAttribute("destination", destination);
        model.addAttribute("seatClass", seatClass);
        model.addAttribute("travelDate", travelDate);
        model.addAttribute("sort", sort);
        return "addons";
    }

    /** Step 3: results with add-ons folded in, plus filtering and pagination. */
    @GetMapping("/results")
    public String results(@RequestParam(required = false) String source,
                          @RequestParam(required = false) String destination,
                          @RequestParam(required = false) String seatClass,
                          @RequestParam(required = false) String travelDate,
                          @RequestParam(required = false, defaultValue = "fare") String sort,
                          @RequestParam(required = false, defaultValue = "false") boolean luggage,
                          @RequestParam(required = false, defaultValue = "false") boolean meals,
                          @RequestParam(required = false) String airline,
                          @RequestParam(required = false) BigDecimal minPrice,
                          @RequestParam(required = false) BigDecimal maxPrice,
                          @RequestParam(required = false, defaultValue = "1") int page,
                          @RequestParam(required = false, defaultValue = "20") int size,
                          Model model) {
        SeatClass clazz = parseClass(seatClass);
        List<Flight> flights = "duration".equals(sort)
                ? flightService.searchByDuration(source, destination, clazz)
                : flightService.search(source, destination, clazz);

        AddOns addOns = new AddOns(luggage, meals);

        // All airlines available for this route (before filtering), for the dropdown.
        java.util.List<String> airlines = flights.stream()
                .map(Flight::getAirline)
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());

        // Build rows with totals, then apply airline / price filters.
        List<FlightRow> rows = new java.util.ArrayList<>();
        for (Flight f : flights) {
            BigDecimal fare = f.getFare();
            BigDecimal addOn = addOns.calculateAmount(fare);
            rows.add(new FlightRow(f, addOn, fare.add(addOn)));
        }
        rows = rows.stream()
                .filter(r -> airline == null || airline.isBlank()
                        || r.getFlight().getAirline().equalsIgnoreCase(airline))
                .filter(r -> minPrice == null || r.getTotal().compareTo(minPrice) >= 0)
                .filter(r -> maxPrice == null || r.getTotal().compareTo(maxPrice) <= 0)
                .collect(java.util.stream.Collectors.toList());

        // Pagination.
        int pageSize = Math.max(5, Math.min(size, 100));
        int totalRows = rows.size();
        int totalPages = Math.max(1, (int) Math.ceil(totalRows / (double) pageSize));
        int currentPage = Math.max(1, Math.min(page, totalPages));
        int from = (currentPage - 1) * pageSize;
        int to = Math.min(from + pageSize, totalRows);
        List<FlightRow> pageRows = rows.subList(from, to);

        model.addAttribute("rows", pageRows);
        model.addAttribute("airlines", airlines);
        model.addAttribute("source", source);
        model.addAttribute("destination", destination);
        model.addAttribute("seatClass", seatClass);
        model.addAttribute("travelDate", travelDate);
        model.addAttribute("sort", sort);
        model.addAttribute("luggage", luggage);
        model.addAttribute("meals", meals);
        model.addAttribute("airline", airline);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("page", currentPage);
        model.addAttribute("size", pageSize);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRows", totalRows);
        model.addAttribute("from", totalRows == 0 ? 0 : from + 1);
        model.addAttribute("to", to);
        return "results";
    }

    private SeatClass parseClass(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return SeatClass.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
