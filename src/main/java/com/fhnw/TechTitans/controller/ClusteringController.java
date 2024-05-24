package com.fhnw.TechTitans.controller;

import com.fhnw.TechTitans.model.Order;
import com.fhnw.TechTitans.service.OrderClusteringService;
import com.fhnw.TechTitans.service.OrderDistancesService;
import com.fhnw.TechTitans.service.OrderService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class ClusteringController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDistancesService orderDistancesService;

    @Autowired
    private OrderClusteringService orderClusteringService;

    private static final Logger logger = LogManager.getLogger(ClusteringController.class);

    @GetMapping("/calculate-distances")
    @ResponseBody
    public double[][] calculateAllDistances() {
        List<Order> orders = orderService.getAllOrders();
        logger.info("Calculating distances for all orders: {}", orders.stream().map(Order::getId).toList());
        return orderDistancesService.calculateAllDistances(orders);
    }

    @GetMapping("/cluster-orders")
    @ResponseBody
    public List<List<Order>> clusterOrders() {
        List<Order> orders = orderService.getAllOrders();
        logger.info("Clustering orders: {}", orders.stream().map(Order::getId).toList());
        return orderClusteringService.clusterOrders(orders);
    }

    @GetMapping("/clustering-page")
    public String clusteringPage() {
        return "clustering";
    }
}
