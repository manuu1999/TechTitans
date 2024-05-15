package com.example.TechTitans.controller;

import com.example.TechTitans.model.Administrator;
import com.example.TechTitans.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.example.TechTitans.model.Customer;
import com.example.TechTitans.model.TruckDriver;

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
            return "redirect:/dashboard";  // Adjust to actual dashboard path
        } else {
            model.addAttribute("error", "Email or Password is incorrect");
            return "login";  // Adjust to your login page
        }
    }
}
