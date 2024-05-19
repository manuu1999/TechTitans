package com.fhnw.TechTitans.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/shipper")
public class ShipperController {

    @GetMapping("/shipments")
    public String getShipmentsHTML() {
        return "shipments";
    }


}
