package com.fhnw.TechTitans.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String getLoginRegisterHTML() {
        return "Login_Register";
    }

    @GetMapping("/login")
    public String getLoginHTML() {
        return "login";
    }
}