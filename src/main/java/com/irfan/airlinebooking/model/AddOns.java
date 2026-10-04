package com.irfan.airlinebooking.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Optional extras a passenger can attach to a booking.
 * <ul>
 *   <li><b>Extra luggage</b> — adds 10% of the class fare.</li>
 *   <li><b>Meals</b> — a flat ₹500.</li>
 * </ul>
 * Encapsulates the pricing rule so both the results page and the booking
 * service compute the same total.
 */
public class AddOns {
    /** Extra-luggage surcharge as a fraction of the class fare. */
    public static final BigDecimal LUGGAGE_RATE = new BigDecimal("0.10");
    /** Flat meal charge in INR. */
    public static final BigDecimal MEAL_CHARGE = new BigDecimal("500");

    private boolean extraLuggage;
    private boolean meals;

    public AddOns() {
    }

    public AddOns(boolean extraLuggage, boolean meals) {
        this.extraLuggage = extraLuggage;
        this.meals = meals;
    }

    /** Total add-on amount for a given class fare. */
    public BigDecimal calculateAmount(BigDecimal classFare) {
        BigDecimal total = BigDecimal.ZERO;
        if (extraLuggage) {
            total = total.add(classFare.multiply(LUGGAGE_RATE));
        }
        if (meals) {
            total = total.add(MEAL_CHARGE);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isNone() {
        return !extraLuggage && !meals;
    }

    public boolean isExtraLuggage() {
        return extraLuggage;
    }

    public void setExtraLuggage(boolean extraLuggage) {
        this.extraLuggage = extraLuggage;
    }

    public boolean isMeals() {
        return meals;
    }

    public void setMeals(boolean meals) {
        this.meals = meals;
    }

    /** Human-readable summary, e.g. "Extra luggage, Meals". */
    public String describe() {
        StringBuilder sb = new StringBuilder();
        if (extraLuggage) {
            sb.append("Extra luggage");
        }
        if (meals) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append("Meals");
        }
        return sb.length() == 0 ? "None" : sb.toString();
    }
}
