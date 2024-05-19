package com.fhnw.TechTitans.controller;

import com.fhnw.TechTitans.model.User;
import com.fhnw.TechTitans.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Controller
public class AuthenticationController {

    private static final Logger logger = LogManager.getLogger(AuthenticationController.class);

    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String getRegisterHTML(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user, BindingResult bindingResult, Model model) {
        logger.debug("POST /register accessed");
        logger.info("Registering user: " + user.getUsername() + " with email: " + user.getEmail() + " and password: " + user.getPassword());

        // Check if username already exists
        if (userService.userExists(user.getUsername(), null)) {
            logger.debug("Username already exists");
            bindingResult.rejectValue("username", "error.user", "An account already exists for this username.");
        }

        //Check if the username is shorter than 3 characters
        if (user.getUsername().length() < 3) {
            logger.debug("Username too short");
            bindingResult.rejectValue("username", "error.username", "Username must be at least 3 characters long.");
        }

        // Check if email is already in use
        if (userService.userExists(null, user.getEmail())) {
            logger.debug("Email already in use");
            bindingResult.rejectValue("email", "error.email", "An account already exists for this email.");
        }

        // Check if password is too short
        if (user.getPassword().length() < 8) {
            logger.debug("Password too short");
            bindingResult.rejectValue("password", "error.password", "Password must be at least 8 characters long.");
        }

        if (bindingResult.hasErrors()) {
            logger.debug("Validation errors present");
            model.addAttribute("user", user);
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "register";
        } else {
            logger.debug("No validation errors, saving user");
            userService.saveUser(user, passwordEncoder());
            return "redirect:/login";
        }
    }


    @GetMapping("/login")
    public String getLoginHTML() {
        return "login";
    }

    @RequestMapping(value="/logout", method = RequestMethod.GET)
    public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return "redirect:/login";
    }
}
