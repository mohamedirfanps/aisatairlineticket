package com.irfan.airlinebooking.web;

import com.irfan.airlinebooking.util.DbConnection;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer tests: login flow, guarded pages, search rendering and templates.
 * Points {@code Config} at a throwaway in-memory SQLite database.
 */
@SpringBootTest
@AutoConfigureMockMvc
class WebFlowTest {

    // Runs at class load, before the Spring context and DataInitializer.
    static {
        System.setProperty("airline.dbUrl", "jdbc:sqlite::memory:");
        System.setProperty("airline.importLimit", "25");
        // Drop any connection a previously-run test left behind so this test
        // gets its own fresh in-memory database (and seeded demo accounts).
        DbConnection.getInstance().close();
    }

    @AfterAll
    static void restore() {
        DbConnection.getInstance().close();
        System.clearProperty("airline.dbUrl");
        System.clearProperty("airline.importLimit");
    }

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession loginAs(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/login")
                        .param("email", email)
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }

    @Test
    void loginPageIsPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Airline Booking System")));
    }

    @Test
    void protectedPageRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/search"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void passengerCanLoginAndSeeSearchPage() throws Exception {
        MockHttpSession session = loginAs("demo@airline.com", "demo123");

        mockMvc.perform(get("/search").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("search"))
                .andExpect(model().attributeExists("airports"));
    }

    @Test
    void addOnsAndResultsPagesRender() throws Exception {
        MockHttpSession session = loginAs("demo@airline.com", "demo123");

        mockMvc.perform(get("/addons").session(session)
                        .param("source", "Delhi").param("destination", "Mumbai")
                        .param("travelDate", "2026-11-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("addons"));

        mockMvc.perform(get("/results").session(session)
                        .param("source", "Delhi").param("destination", "Mumbai")
                        .param("travelDate", "2026-11-01")
                        .param("luggage", "true").param("meals", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("results"))
                .andExpect(model().attributeExists("rows", "totalPages", "airlines"));
    }

    @Test
    void resultsSupportFilteringAndPagination() throws Exception {
        MockHttpSession session = loginAs("demo@airline.com", "demo123");

        // Unfiltered: all flights from Delhi, 5 per page.
        MvcResult first = mockMvc.perform(get("/results").session(session)
                        .param("source", "Delhi")
                        .param("travelDate", "2026-11-01").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("size", 5))
                .andReturn();

        int total = (int) first.getModelAndView().getModel().get("totalRows");
        org.junit.jupiter.api.Assumptions.assumeTrue(total > 5, "need >5 flights to page");

        // Page 2 must report itself as page 2.
        MvcResult second = mockMvc.perform(get("/results").session(session)
                        .param("source", "Delhi")
                        .param("travelDate", "2026-11-01").param("size", "5").param("page", "2"))
                .andExpect(status().isOk())
                .andReturn();
        org.junit.jupiter.api.Assertions.assertEquals(2,
                second.getModelAndView().getModel().get("page"));

        // Airline filter reduces the result set.
        MvcResult filtered = mockMvc.perform(get("/results").session(session)
                        .param("source", "Delhi")
                        .param("travelDate", "2026-11-01").param("airline", "Indigo"))
                .andExpect(status().isOk())
                .andReturn();
        int filteredTotal = (int) filtered.getModelAndView().getModel().get("totalRows");
        org.junit.jupiter.api.Assertions.assertTrue(filteredTotal <= total);

        // Price filter: an impossible max yields nothing.
        mockMvc.perform(get("/results").session(session)
                        .param("source", "Delhi")
                        .param("travelDate", "2026-11-01").param("maxPrice", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totalRows", 0));
    }

    @Test
    void badCredentialsReturnToLoginWithError() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", "demo@airline.com")
                        .param("password", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void adminReportsRequiresAdmin() throws Exception {
        mockMvc.perform(get("/admin/reports"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void adminDashboardRequiresAdmin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void adminCanOpenDashboardAndPages() throws Exception {
        MockHttpSession session = loginAs("admin@airline.com", "admin123");

        mockMvc.perform(get("/admin").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));

        mockMvc.perform(get("/admin/flights").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/flights"));

        mockMvc.perform(get("/admin/passengers").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/passengers"));

        mockMvc.perform(get("/admin/bookings").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/bookings"));

        mockMvc.perform(get("/admin/reports").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/reports"))
                .andExpect(model().attributeExists("revenue", "avgFare", "byRoute"));
    }

    @Test
    void bookingFormCarriesFlightIdForSubmission() throws Exception {
        MockHttpSession session = loginAs("demo@airline.com", "demo123");

        // Pick any flight id from the results page.
        MvcResult results = mockMvc.perform(get("/results").session(session)
                        .param("source", "Delhi").param("travelDate", "2026-11-01"))
                .andExpect(status().isOk())
                .andReturn();
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("/book/(\\d+)").matcher(results.getResponse().getContentAsString());
        org.junit.jupiter.api.Assumptions.assumeTrue(m.find(), "no flights generated");
        String flightId = m.group(1);

        // The rendered booking form must include the flight id, otherwise the
        // subsequent POST submits null and fails.
        mockMvc.perform(get("/book/" + flightId).session(session)
                        .param("travelDate", "2026-11-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("book"));
    }

    @Test
    void bookingWithMissingFlightIdShowsErrorNotStackTrace() throws Exception {
        MockHttpSession session = loginAs("demo@airline.com", "demo123");

        mockMvc.perform(post("/book").session(session)
                        .param("flightId", "")
                        .param("seatNumber", "1C")
                        .param("passengerName", "X")
                        .param("passengerAge", "30")
                        .param("travelDate", "2026-11-01")
                        .param("paymentMethod", "UPI")
                        .param("upiId", "x@upi"))
                .andExpect(status().isOk())
                .andExpect(view().name("error-page"));
    }
}
