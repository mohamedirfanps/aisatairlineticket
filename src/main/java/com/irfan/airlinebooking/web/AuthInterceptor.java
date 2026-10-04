package com.irfan.airlinebooking.web;

import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Guards authenticated areas: anything other than the public login page
 * redirects to {@code /login} until a user is in the session.
 */
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (!SessionUser.isLoggedIn(request)) {
            response.sendRedirect("/login");
            return false;
        }
        return true;
    }
}
