package com.fhnw.TechTitans.controller;

import com.fhnw.TechTitans.model.*;
import com.fhnw.TechTitans.repository.OrderClusterRepository;
import com.fhnw.TechTitans.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class ClusteringController {

    public static final float MAX_CAPACITY_M3 = 150.0f; // Maximum volume capacity of a truck in cubic meters
    public static final float MAX_WEIGHT = 150.0f; // Maximum weight capacity of a truck in kilograms

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TruckService truckService;

    @Autowired
    private DepotService depotService;

    @Autowired
    private OrderClusterRepository orderClusterRepository;

    Logger logger = LogManager.getLogger(ClusteringController.class);

    @GetMapping("/clustering")
    public String getClusteringSite(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userService.findByUsername(auth.getName());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("users", userService.getAllUsers());
        return "clustering"; // Name of the Thymeleaf template
    }

    @GetMapping("/clusterOrders")
    @ResponseBody
    public String clusterOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<Depot> depots = depotService.getAllDepots();
        List<Truck> trucks = truckService.getAllTrucks();

        logger.info("Starting order clustering process...");
        logger.debug("Total orders: {}, Total depots: {}, Total trucks: {}", orders.size(), depots.size(), trucks.size());

        if (orders.isEmpty() || depots.isEmpty() || trucks.isEmpty()) {
            logger.warn("No orders, depots, or trucks available for clustering.");
            return "No orders, depots, or trucks available for clustering.";
        }

        List<OrderCluster> orderClusters = new ArrayList<>();
        for (Depot depot : depots) {
            logger.info("Processing depot: {}", depot.getId());
            PriorityQueue<Order> orderQueue = new PriorityQueue<>(Comparator.comparingDouble(order -> calculateDistance(order, depot)));

            for (Order order : orders) {
                if (!order.isInCluster()) {
                    orderQueue.offer(order);
                }
            }

            while (!orderQueue.isEmpty()) {
                List<Order> clusterOrders = new ArrayList<>();
                float totalVolume = 0.0f;
                float totalWeight = 0.0f;

                while (!orderQueue.isEmpty() && totalVolume <= MAX_CAPACITY_M3 && totalWeight <= MAX_WEIGHT) {
                    Order order = orderQueue.peek();
                    if (totalVolume + order.getVolume() <= MAX_CAPACITY_M3 && totalWeight + order.getWeight() <= MAX_WEIGHT) {
                        clusterOrders.add(orderQueue.poll());
                        totalVolume += order.getVolume();
                        totalWeight += order.getWeight();
                    } else {
                        break;
                    }
                }

                if (!clusterOrders.isEmpty()) {
                    logger.debug("Cluster created with orders: {}", clusterOrders.size());
                    Truck assignedTruck = findAvailableTruck(trucks, depot);
                    if (assignedTruck != null) {
                        logger.info("Truck assigned: {}", assignedTruck.getId());
                        OrderCluster orderCluster = new OrderCluster();
                        orderCluster.setOrders(clusterOrders);
                        orderCluster.setDepot(depot);
                        orderCluster.setTruck(assignedTruck);
                        orderCluster.setInCluster(true);

                        orderClusters.add(orderCluster);

                        for (Order order : clusterOrders) {
                            order.setInCluster(true);
                            orderService.saveOrder(order);
                        }
                    } else {
                        logger.warn("No available truck found for depot: {}", depot.getId());
                    }
                }
            }
        }

        orderClusterRepository.saveAll(orderClusters);
        logger.info("Order clustering process completed.");
        return "Orders have been successfully clustered and assigned to trucks.";
    }

    private double calculateDistance(Order order, Depot depot) {
        double lat1 = order.getLatitude();
        double lon1 = order.getLongitude();
        double lat2 = depot.getLatitude();
        double lon2 = depot.getLongitude();
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                + Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // Convert to kilometers

        logger.debug("Calculated distance between order {} and depot {}: {} km", order.getId(), depot.getId(), distance);
        return distance;
    }

    private Truck findAvailableTruck(List<Truck> trucks, Depot depot) {
        for (Truck truck : trucks) {
            if (truck.getDepot() != null && truck.getDepot().getId().equals(depot.getId()) && truck.isAvailable()) {
                truck.setAvailable(false); // Mark the truck as unavailable
                truckService.saveTruck(truck);
                logger.debug("Truck {} marked as unavailable", truck.getId());
                return truck;
            }
        }
        logger.warn("No available truck found for depot: {}", depot.getId());
        return null; // No available truck found
    }
}
