package com.example.TechTitans.controller;

import com.example.TechTitans.model.Administrator;
import com.example.TechTitans.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
        Administrator administrator = authenticationService.login(email, password);
        if (administrator != null) {
            return "redirect:/dashboard";  // Assuming a dashboard page
        } else {
            model.addAttribute("error", "Email or Password is incorrect");
            return "login";
        }
    }
}
