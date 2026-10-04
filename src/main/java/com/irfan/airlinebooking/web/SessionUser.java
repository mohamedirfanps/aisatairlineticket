package com.irfan.airlinebooking.web;

import com.irfan.airlinebooking.enums.Role;
import com.irfan.airlinebooking.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** Helpers for keeping the logged-in {@link User} in the HTTP session. */
public final class SessionUser {

    public static final String KEY = "currentUser";

    private SessionUser() {
    }

    public static void set(HttpServletRequest request, User user) {
        request.getSession(true).setAttribute(KEY, user);
    }

    public static User get(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (User) session.getAttribute(KEY);
    }

    public static void clear(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(KEY);
        }
    }

    public static boolean isAdmin(HttpServletRequest request) {
        User user = get(request);
        return user != null && user.getRole() == Role.ADMIN;
    }

    public static boolean isLoggedIn(HttpServletRequest request) {
        return get(request) != null;
    }
}
