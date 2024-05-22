package com.fhnw.TechTitans.controller;


import com.fhnw.TechTitans.model.User;
import com.fhnw.TechTitans.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/shipper")
public class ShipperController {

    @Autowired
    private UserService userService;

    @GetMapping("/shipments")
    public String getShipmentsHTML(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            User user = userService.findByUsername(userDetails.getUsername());
            model.addAttribute("username", user.getUsername());
            model.addAttribute("role", user.getRole().name());
            if(user.getRole().name().equals("SHIPPER")){
                return "shipments";
            }
        }
        return "redirect:/";
    }

    @PostMapping("/submitShipment")
    public String submitShipmentHTML() {
        //TODO: Implement
        return null;
    }


}
