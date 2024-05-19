package com.fhnw.TechTitans.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/trucker")
public class TruckerController {

    @GetMapping("/deliveries")
    public String getDeliveriesHTML() {
        return "deliveries";
    }

}
