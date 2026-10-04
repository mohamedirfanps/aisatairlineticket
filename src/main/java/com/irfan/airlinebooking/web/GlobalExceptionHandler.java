package com.irfan.airlinebooking.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/** Turns domain exceptions into a friendly error page instead of a stack trace. */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public String handle(RuntimeException ex, HttpServletRequest request, Model model) {
        model.addAttribute("message", ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
        model.addAttribute("path", request.getRequestURI());
        return "error-page";
    }
}
