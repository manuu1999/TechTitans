package com.fhnw.TechTitans.controller;

import com.fhnw.TechTitans.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/login")
    public String login(@RequestParam("email") String email,
                        @RequestParam("password") String password,
                        @RequestParam("role") String role,
                        Model model) {
        Object user = null;
        switch (role.toLowerCase()) {
            case "administrator":
                user = authenticationService.loginAsAdministrator(email, password);
                break;
            case "customer":
                user = authenticationService.loginAsCustomer(email, password);
                break;
            case "truckdriver":
                user = authenticationService.loginAsTruckDriver(email, password);
                break;
        }

        if (user != null) {
            return "Home";  // New page
        } else {
            model.addAttribute("error", "Email or Password is incorrect");
            return "Login_Register";
        }
    }
}
