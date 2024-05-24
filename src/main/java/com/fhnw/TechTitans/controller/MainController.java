package com.fhnw.TechTitans.controller;

import com.fhnw.TechTitans.model.User;
import com.fhnw.TechTitans.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class MainController {

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @GetMapping("/")
    public String getMainPage(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            String username = userDetails.getUsername();
            logger.debug("UserDetails is not null. Username: {}", username);

            User user = userService.findByUsername(username);
            if (user != null) {
                logger.debug("User found. Username: {}", user.getUsername());
                model.addAttribute("username", user.getUsername());
                model.addAttribute("role", user.getRole().name());
            } else {
                logger.error("No user found with username: {}", username);
                // Handle the case where user is null (e.g., return an error page or a message)
                return "error/404";
            }
        } else {
            logger.warn("UserDetails is null.");
        }
        return "home";
    }
}
