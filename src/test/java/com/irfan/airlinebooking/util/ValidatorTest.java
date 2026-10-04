package com.irfan.airlinebooking.util;

import com.irfan.airlinebooking.exception.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Tests for input validation helpers. */
class ValidatorTest {

    @Test
    void emailIsValidated() {
        assertEquals("a@b.com", Validator.requireEmail(" a@b.com "));
        assertThrows(ValidationException.class, () -> Validator.requireEmail("not-an-email"));
    }

    @Test
    void blankTextIsRejected() {
        assertThrows(ValidationException.class, () -> Validator.requireText("  ", "Name"));
    }

    @Test
    void positiveIntIsEnforced() {
        assertEquals(5, Validator.requirePositiveInt("5", "Age"));
        assertThrows(ValidationException.class, () -> Validator.requirePositiveInt("0", "Age"));
        assertThrows(ValidationException.class, () -> Validator.requirePositiveInt("abc", "Age"));
    }
}
