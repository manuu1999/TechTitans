package com.fhnw.TechTitans.controller;

import com.fhnw.TechTitans.model.User;
import com.fhnw.TechTitans.model.Order;
import com.fhnw.TechTitans.model.Truck;
import com.fhnw.TechTitans.model.OrderCluster;
import com.fhnw.TechTitans.service.UserService;
import com.fhnw.TechTitans.service.ClusteringService;
import com.fhnw.TechTitans.service.OrderService;
import com.fhnw.TechTitans.service.TruckService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ClusteringService clusteringService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TruckService truckService;

    Logger logger = LogManager.getLogger(AdminController.class);

    @GetMapping("/manageSite")
    public String getManageSite(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("users", userService.getAllUsers());
        return "manageSite";
    }

    @PostMapping("/manageSite/updateRole")
    public String updateUserRole(@RequestParam("userId") Long userId, @RequestParam("role") String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        if (!currentUser.getId().equals(userId)) {
            userService.updateUserRole(userId, role);
        } else {
            logger.warn("User " + currentUser.getUsername() + " tried to change their own role.");
        }
        return "redirect:/manageSite";
    }

    @PostMapping("/manageSite/deleteUser")
    public String deleteUser(@RequestParam("userId") Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        if (!currentUser.getId().equals(userId)) {
            userService.deleteUser(userId);
        } else {
            logger.warn("User " + currentUser.getUsername() + " tried to delete their own account.");
        }
        return "redirect:/manageSite";
    }

    @GetMapping("/manageSite/clusterOrders")
    public String clusterOrders(Model model) {
        try {
            List<Order> orders = orderService.getAllOrders();
            List<Truck> trucks = truckService.getAllTrucks();
            List<OrderCluster> clusters = clusteringService.clusterOrders(orders, trucks);
            model.addAttribute("clusters", clusters);
        } catch (Exception e) {
            logger.error("Error clustering orders", e);
            model.addAttribute("error", "There was an error clustering the orders. Please try again.");
        }
        return "manageSite";
    }
}
