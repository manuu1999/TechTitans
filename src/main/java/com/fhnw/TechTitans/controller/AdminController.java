package com.fhnw.TechTitans.controller;

import com.fhnw.TechTitans.model.Product;
import com.fhnw.TechTitans.model.User;
import com.fhnw.TechTitans.service.ProductService;
import com.fhnw.TechTitans.service.UserService;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    Logger logger = LogManager.getLogger(AdminController.class);

    @GetMapping("/manageSite")
    public String getManageSite(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("products", productService.findAll());
        model.addAttribute("product", new Product());
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

    // Display form for adding a new product
    @GetMapping("/addProduct")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product());
        return "addProduct";
    }

    // Handle form submission for adding a new product
    @PostMapping("/addProduct")
    public String addProduct(Product product) {
        productService.save(product);
        return "redirect:/admin/manageSite";
    }

    // Display form for editing an existing product
    @GetMapping("/editProduct/{id}")
    public String showEditProductForm(@PathVariable("id") Integer id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        return "editProduct";
    }

    // Handle form submission for editing an existing product
    @PostMapping("/editProduct")
    public String editProduct(Product product) {
        productService.save(product);
        return "redirect:/admin/manageSite";
    }

    // Handle form submission for deleting an existing product
    @GetMapping("/deleteProduct/{productId}")
    public String deleteProduct(@PathVariable("productId") Integer productId) {
        productService.deleteProduct(productId);
        return "redirect:/admin/manageSite";
    }
}
