package com.irfan.airlinebooking.enums;

/**
 * Seat classes supported by the system.
 * <p>
 * The source dataset contains four classes ("Economy", "Business",
 * "Premium Economy", "First"). To keep the domain model aligned with the
 * syllabus we collapse them into two bookable classes:
 * <ul>
 *   <li>Economy  &lt;- Economy, Premium Economy</li>
 *   <li>Business &lt;- Business, First</li>
 * </ul>
 */
public enum SeatClass {
    ECONOMY,
    BUSINESS;

    /** Maps a raw dataset class label onto one of our two classes. */
    public static SeatClass fromDataset(String raw) {
        if (raw == null) {
            return ECONOMY;
        }
        return switch (raw.trim().toLowerCase()) {
            case "business", "first" -> BUSINESS;
            default -> ECONOMY;
        };
    }
}
