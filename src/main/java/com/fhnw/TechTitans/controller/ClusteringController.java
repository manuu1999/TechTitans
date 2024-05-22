package com.fhnw.TechTitans.controller;

import com.fhnw.TechTitans.model.Order;
import com.fhnw.TechTitans.model.Truck;
import com.fhnw.TechTitans.model.User;
import com.fhnw.TechTitans.service.ClusteringService;
import com.fhnw.TechTitans.service.OrderService;
import com.fhnw.TechTitans.service.TruckService;
import com.fhnw.TechTitans.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ClusteringController {

    @Autowired
    private UserService userService;

    @Autowired
    private ClusteringService clusteringService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TruckService truckService;

    Logger logger = LogManager.getLogger(ClusteringController.class);

    @GetMapping("/clustering")
    public String getClusteringSite(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("users", userService.getAllUsers());
        return "clustering"; // Name of the Thymeleaf template (clustering.html)
    }

    @GetMapping("/clusterOrders")
    @ResponseBody
    public String clusterOrders() {
        try {
            List<Order> orders = orderService.getAllOrders();
            List<Truck> trucks = truckService.getAllTrucks();
            clusteringService.clusterOrders(orders, trucks);
            return "{\"status\":\"success\"}";
        } catch (Exception e) {
            logger.error("Error clustering orders", e);
            return "{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}";
        }
    }
}
