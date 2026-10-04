package com.irfan.airlinebooking.util;

import com.irfan.airlinebooking.exception.ValidationException;

/** Input validation helpers shared by the UI and services. */
public final class Validator {

    private Validator() {
    }

    public static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field + " cannot be empty");
        }
        return value.trim();
    }

    public static String requireEmail(String value) {
        requireText(value, "Email");
        String v = value.trim();
        if (!v.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ValidationException("Invalid email: " + value);
        }
        return v;
    }

    public static int requireInt(String value, String field) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            throw new ValidationException(field + " must be a whole number");
        }
    }

    public static int requirePositiveInt(String value, String field) {
        int n = requireInt(value, field);
        if (n <= 0) {
            throw new ValidationException(field + " must be greater than zero");
        }
        return n;
    }
}
