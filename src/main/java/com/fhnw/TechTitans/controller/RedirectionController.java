package com.fhnw.TechTitans.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RedirectionController {

    @GetMapping("/deliveries")
    public String redirectToTruckerDeliveries() {
        return "redirect:/trucker/deliveries";
    }

    @GetMapping("/shipments")
    public String redirectToShipperShipments() {
        return "redirect:/shipper/shipments";
    }

    @GetMapping("/manageSite")
    public String redirectToAdminManageSite() {
        return "redirect:/admin/manageSite";
    }
}