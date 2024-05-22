package com.fhnw.TechTitans.controller;

import com.fhnw.TechTitans.model.*;
import com.fhnw.TechTitans.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/shop")
public class CustomerController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerService customerService;

    @GetMapping("/catalogue")
    public String viewCatalogue(Model model) {
        List<Product> products = productService.findAll();
        model.addAttribute("products", products);
        return "catalogue";
    }

    @GetMapping("/basket")
    public String viewBasket(Model model) {
        // Initialize basket if it doesn't exist
        List<OrderProduct> basket = (List<OrderProduct>) model.asMap().get("basket");
        if (basket == null) {
            basket = new ArrayList<>();
            model.addAttribute("basket", basket);
        }
        return "basket";
    }

    @PostMapping("/add-to-basket")
    public String addToBasket(@RequestParam("productId") Integer productId,
                              @RequestParam("quantity") Integer quantity,
                              Model model) {
        // Initialize basket if it doesn't exist
        List<OrderProduct> basket = (List<OrderProduct>) model.asMap().get("basket");
        if (basket == null) {
            basket = new ArrayList<>();
        }

        // Find product and create OrderProduct
        Product product = productService.findById(productId);
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setProduct(product);
        orderProduct.setQuantity(quantity);
        basket.add(orderProduct);

        model.addAttribute("basket", basket);
        return "redirect:/shop/basket";
    }

    @PostMapping("/place-order")
    public String placeOrder(@RequestParam("customerId") Integer customerId,
                             @RequestParam("addressId") Integer addressId,
                             Model model) {
        // Initialize basket if it doesn't exist
        List<OrderProduct> basket = (List<OrderProduct>) model.asMap().get("basket");
        if (basket == null || basket.isEmpty()) {
            // Handle empty basket case
            return "redirect:/shop/catalogue";
        }

        // Find customer and delivery address
        Customer customer = customerService.findById(customerId);
        DeliveryAddresses deliveryAddress = customerService.findAddressById(addressId);

        // Create and save order
        Order order = new Order();
        order.setCustomer(customer);
        order.setDeliveryAddress(deliveryAddress);
        order.setTimestamp(LocalDateTime.now());
        order.setOrderProducts(basket);
        orderService.save(order);

        // Clear the basket
        model.addAttribute("basket", new ArrayList<>());

        return "redirect:/shop/catalogue";
    }
}
