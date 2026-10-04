package com.irfan.airlinebooking.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/** Tests for the CSV parser. */
class CsvReaderTest {

    @Test
    void splitsPlainFields() {
        assertArrayEquals(new String[]{"a", "b", "c"}, CsvReader.parseLine("a,b,c"));
    }

    @Test
    void respectsQuotedCommas() {
        assertArrayEquals(new String[]{"Delhi", "Mumbai", "1,234"},
                CsvReader.parseLine("Delhi,Mumbai,\"1,234\""));
    }

    @Test
    void unescapesDoubledQuotes() {
        assertArrayEquals(new String[]{"He said \"hi\"", "x"},
                CsvReader.parseLine("\"He said \"\"hi\"\"\",x"));
    }
}
