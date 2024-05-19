package com.fhnw.TechTitans.controller;

import com.fhnw.TechTitans.model.User;
import com.fhnw.TechTitans.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

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
}
