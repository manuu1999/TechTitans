package com.fhnw.TechTitans.service;

import com.fhnw.TechTitans.model.Order;
import com.fhnw.TechTitans.model.Truck;
import com.fhnw.TechTitans.model.OrderCluster;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

@Service
public class ClusteringService {
    public static final float MAX_CAPACITY_M3 = 80.0f; // Maximum volume capacity of a truck in cubic meters
    public static final float MAX_WEIGHT = 80.0f; // Maximum weight capacity of a truck in kilograms
    public static final long OLD_ORDER_THRESHOLD = 7 * 24 * 60 * 60 * 1000L; // 7 days in milliseconds

    @Autowired
    private OrderService orderService;

    private static final Logger logger = LogManager.getLogger(ClusteringService.class);

    /**
     * Clusters orders based on their delivery coordinates and truck capacities.
     *
     * @param orders List of orders to be clustered
     * @param trucks List of available trucks
     * @return List of created order clusters
     */
    public List<OrderCluster> clusterOrders(List<Order> orders, List<Truck> trucks) {
        List<OrderCluster> clusters = new ArrayList<>();
        List<Order> remainingOrders = new ArrayList<>();

        // Filter out orders that are already in a cluster
        for (Order order : orders) {
            if (!order.isClustered()) {
                remainingOrders.add(order); // Add orders not in a cluster to the list of remaining orders
            }
        }

        // Sort remaining orders by their timestamp, prioritizing older orders
        remainingOrders.sort(Comparator.comparing(Order::getTimestamp));

        // Main clustering loop
        while (!remainingOrders.isEmpty() && !trucks.isEmpty()) {
            // Find the closest truck to the first remaining order
            Truck closestTruck = findClosestTruck(remainingOrders.get(0), trucks);
            if (closestTruck == null) {
                logger.warn("No closest truck found for the order. Exiting the loop.");
                break; // If no closest truck is found, exit the loop
            }

            // Create a new cluster with the closest truck
            OrderCluster currentCluster = new OrderCluster(closestTruck);
            trucks.remove(closestTruck);  // Remove the truck from the list of available trucks
            logger.info("Created a new cluster with truck ID: {}", closestTruck.getId());

            // Add orders to the current cluster until no more suitable orders can be found
            while (true) {
                Order bestOrder = findBestOrder(currentCluster, remainingOrders);
                if (bestOrder == null) {
                    logger.info("No suitable order found for the current cluster. Exiting the loop.");
                    break; // Exit the loop if no suitable order is found
                }
                splitAndAddOrder(currentCluster, bestOrder, remainingOrders);
            }

            clusters.add(currentCluster); // Add the completed cluster to the list of clusters
            logger.info("Cluster completed and added to the list. Total clusters: {}", clusters.size());
        }

        // Mark orders in the clusters as clustered in the database
        for (OrderCluster cluster : clusters) {
            for (Order order : cluster.getOrders()) {
                orderService.markOrderAsClustered(order);
                logger.info("Order ID: {} marked as clustered", order.getId());
            }
        }

        // Print the details of the created clusters
        printOrderClusters(clusters);

        return clusters;
    }

    /**
     * Finds the closest truck to the given order based on delivery coordinates.
     *
     * @param order The order for which to find the closest truck
     * @param trucks List of available trucks
     * @return The closest truck to the order or null if no truck is found
     */
    private Truck findClosestTruck(Order order, List<Truck> trucks) {
        Truck closestTruck = null;
        double closestDistance = Double.MAX_VALUE;

        // Iterate over each truck to find the closest one
        for (Truck truck : trucks) {
            double distance = calculateDistance(order.getDeliveryLatitude(), order.getDeliveryLongitude(), truck.getLatitude(), truck.getLongitude());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestTruck = truck; // Update the closest truck
            }
        }

        logger.info("Closest truck ID: {} found for order ID: {}", closestTruck != null ? closestTruck.getId() : null, order.getId());
        return closestTruck;
    }

    /**
     * Splits an order if it exceeds the cluster's capacity or weight limits and adds it to the cluster.
     *
     * @param cluster The cluster to which the order should be added
     * @param order The order to be split and added
     * @param remainingOrders List of remaining orders to be processed
     */
    private void splitAndAddOrder(OrderCluster cluster, Order order, List<Order> remainingOrders) {
        // Split the order if its volume or weight exceeds the maximum limits
        while (order.getTotalVolume() > MAX_CAPACITY_M3 || order.getTotalWeight() > MAX_WEIGHT) {
            Order partialOrder = orderService.splitOrder(order, MAX_CAPACITY_M3 - cluster.getTotalVolume(), MAX_WEIGHT - cluster.getTotalWeight());
            cluster.addOrder(partialOrder);
            orderService.markOrderAsClustered(partialOrder);
            logger.info("Partial order ID: {} added to cluster with truck ID: {}", partialOrder.getId(), cluster.getTruck().getId());
        }
        cluster.addOrder(order); // Add the remaining part of the order to the cluster
        remainingOrders.remove(order); // Remove the order from the list of remaining orders
        orderService.markOrderAsClustered(order); // Ensure the order is marked as clustered
        logger.info("Order ID: {} added to cluster with truck ID: {}", order.getId(), cluster.getTruck().getId());
    }

    /**
     * Finds the best order to add to the current cluster based on proximity and capacity constraints.
     *
     * @param cluster The current cluster
     * @param remainingOrders List of remaining orders to be processed
     * @return The best order to add to the cluster or null if no suitable order is found
     */
    private Order findBestOrder(OrderCluster cluster, List<Order> remainingOrders) {
        PriorityQueue<Order> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(o -> calculateDistance(cluster.getCenterLatitude(), cluster.getCenterLongitude(), o.getDeliveryLatitude(), o.getDeliveryLongitude())));

        // Add orders that fit within the cluster's capacity and weight limits to the priority queue
        for (Order order : remainingOrders) {
            if (cluster.getTotalVolume() + order.getTotalVolume() <= MAX_CAPACITY_M3 && cluster.getTotalWeight() + order.getTotalWeight() <= MAX_WEIGHT) {
                priorityQueue.add(order);
            }
        }

        // Return the closest order or null if the priority queue is empty
        Order bestOrder = priorityQueue.isEmpty() ? null : priorityQueue.poll();
        logger.info("Best order ID: {} found for cluster with truck ID: {}", bestOrder != null ? bestOrder.getId() : null, cluster.getTruck().getId());
        return bestOrder;
    }

    /**
     * Calculates the distance between two geographical points using the Haversine formula.
     *
     * @param lat1 Latitude of the first point
     * @param lon1 Longitude of the first point
     * @param lat2 Latitude of the second point
     * @param lon2 Longitude of the second point
     * @return The distance between the two points in kilometers
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in kilometers
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        logger.info("Calculated distance: {} km between points ({}, {}) and ({}, {})", distance, lat1, lon1, lat2, lon2);
        return distance;
    }

    /**
     * Prints the details of the created order clusters.
     *
     * @param clusters List of order clusters to be printed
     */
    public static void printOrderClusters(List<OrderCluster> clusters) {
        // Iterate over each cluster and print its details
        for (int i = 0; i < clusters.size(); i++) {
            OrderCluster cluster = clusters.get(i);
            logger.info("Cluster {} (Truck {}):", (i + 1), cluster.getTruck().getId());
            for (Order order : cluster.getOrders()) {
                logger.info("  Order ID: {}, Volume: {}, Weight: {}, Delivery Latitude: {}, Delivery Longitude: {}",
                        order.getId(), order.getTotalVolume(), order.getTotalWeight(), order.getDeliveryLatitude(), order.getDeliveryLongitude());
            }
        }
    }
}
