package com.example.TechTitans.controller;

import org.springframework.web.bind.annotation.GetMapping;

@org.springframework.stereotype.Controller
public class Controller {

    @GetMapping("/")
    public String getLogin_RegisterHTML() {
        return "Login_Register";
    }
}
