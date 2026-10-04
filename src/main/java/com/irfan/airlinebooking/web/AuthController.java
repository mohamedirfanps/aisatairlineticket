package com.irfan.airlinebooking.web;

import com.irfan.airlinebooking.model.User;
import com.irfan.airlinebooking.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Login, registration and logout. */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home(HttpServletRequest request) {
        if (!SessionUser.isLoggedIn(request)) {
            return "redirect:/login";
        }
        return SessionUser.isAdmin(request) ? "redirect:/admin/flights" : "redirect:/search";
    }

    @GetMapping("/login")
    public String loginForm(HttpServletRequest request) {
        return SessionUser.isLoggedIn(request) ? "redirect:/" : "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpServletRequest request,
                        Model model) {
        try {
            User user = userService.login(email, password);
            SessionUser.set(request, user);
            return "redirect:/";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("email", email);
            return "login";
        }
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam(required = false) String phone,
                           Model model) {
        try {
            userService.register(name, email, password, phone);
            model.addAttribute("message", "Registration successful. Please log in.");
            return "login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            model.addAttribute("phone", phone);
            return "register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        SessionUser.clear(request);
        return "redirect:/login";
    }
}
