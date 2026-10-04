package com.irfan.airlinebooking.ui;

import java.util.List;
import java.util.Scanner;

/** Small helper around {@link Scanner} for consistent console input. */
public final class Console {
    private final Scanner scanner = new Scanner(System.in);

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    }

    public int readInt(String prompt) {
        while (true) {
            String raw = readLine(prompt);
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                System.out.println("  ! Please enter a number.");
            }
        }
    }

    public void printHeader(String title) {
        String line = "=".repeat(Math.max(44, title.length() + 8));
        System.out.println();
        System.out.println(line);
        System.out.println("  " + title);
        System.out.println(line);
    }

    public void printMenu(String... options) {
        for (int i = 0; i < options.length; i++) {
            System.out.printf("  %d) %s%n", i + 1, options[i]);
        }
        System.out.println();
    }

    public void pause() {
        readLine("  Press Enter to continue...");
    }

    /** Prints a simple list of pre-formatted rows. */
    public void printTable(String header, List<String> rows) {
        if (rows.isEmpty()) {
            System.out.println("  (no results)");
            return;
        }
        System.out.println(header);
        for (String row : rows) {
            System.out.println(row);
        }
    }
}
