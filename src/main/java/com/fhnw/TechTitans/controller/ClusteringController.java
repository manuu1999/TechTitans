package com.fhnw.TechTitans.controller;

import com.fhnw.TechTitans.model.ClusterAssignment;
import com.fhnw.TechTitans.model.Order;
import com.fhnw.TechTitans.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ClusteringController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDistancesService orderDistancesService;

    @Autowired
    private OrderClusteringService orderClusteringService;

    @Autowired
    private ClusterAssignmentService clusterAssignmentService;

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

    @GetMapping("/clustering")
    public String clusteringPage() {
        return "clustering";
    }

    @GetMapping("/api/clusters")
    @ResponseBody
    public List<ClusterDTO> getClusters() {
        logger.info("Fetching clusters with assigned trucks and orders.");
        return clusterAssignmentService.getAllClusterAssignments();
    }
}
