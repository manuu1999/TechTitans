package com.fhnw.TechTitans.service;

import com.fhnw.TechTitans.model.Order;
import com.fhnw.TechTitans.model.Truck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class OrderClusteringService {

    // Logger for logging information
    private static final Logger logger = Logger.getLogger(OrderClusteringService.class.getName());

    // Injecting dependencies using @Autowired
    @Autowired
    private OrderDistancesService orderDistancesService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TruckService truckService;

    @Autowired
    private ClusterAssignmentService clusterAssignmentService;

    @Autowired
    private AreaCalculatorService areaCalculatorService;
    @Autowired
    private DistanceCalculatorService distanceCalculatorService;

    // Constants for maximum weight, volume, and area
    private static final double MAX_WEIGHT = 6000.0;
    private static final double MAX_VOLUME = 40.0;
    private static final double MAX_AREA = 1500.0; // Max area in square kilometers
    private static final double MAX_DISTANCE = 150.0; // Max distance in kilometers

    // Method to cluster orders
    public List<List<Order>> clusterOrders(List<Order> orders) {
        // Filter out orders that are already in clusters
        orders = orders.stream()
                .filter(order -> !order.isInCluster())
                .collect(Collectors.toList());

        // Return empty list if there are no orders left to cluster
        if (orders.isEmpty()) {
            return new ArrayList<>();
        }

        // Gets all Trucks with the status "AVAILABLE" - function from service class
        List<Truck> availableTrucks = truckService.getAvailableTrucks();
        // calculates the amount of trucks - to know max amount of clusters that we can create
        int availableTruckCount = availableTrucks.size();

        // Return empty list if there are no available trucks
        if (availableTruckCount == 0) {
            return new ArrayList<>();
        }

        // Calculate distances between all orders - method from OrderDistancesService class
        double[][] distances = orderDistancesService.calculateAllDistances(orders);
        // get the total count of orders
        int size = orders.size();
        List<List<Order>> clusters = new ArrayList<>();
        // array of boolean values with an equal size to the count of orders
        // used to check if an order is already clustered
        boolean[] clustered = new boolean[size];

        // Find the order that is furthest from the center point
        Order furthestOrder = orderDistancesService.findFurthestOrderFromCenter(orders);
        int furthestOrderIndex = orders.indexOf(furthestOrder);

        // Find the closest pair to the furthest order
        int closestOrderIndex1 = -1;
        int closestOrderIndex2 = -1;
        double closestDistance = Double.MAX_VALUE;

        for (int i = 0; i < size; i++) {
            if (i != furthestOrderIndex && !clustered[i]) {
                double distance = distances[furthestOrderIndex][i];
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestOrderIndex1 = furthestOrderIndex;
                    closestOrderIndex2 = i;
                }
            }
        }

        // Create the initial cluster with the furthest order and its closest pair
        if (closestOrderIndex1 != -1 && closestOrderIndex2 != -1) {
            List<Order> newCluster = new ArrayList<>();
            newCluster.add(orders.get(closestOrderIndex1));
            newCluster.add(orders.get(closestOrderIndex2));
            clustered[closestOrderIndex1] = true;
            clustered[closestOrderIndex2] = true;

            // Mark orders as clustered in the order service
            orderService.markOrderAsClustered(orders.get(closestOrderIndex1));
            orderService.markOrderAsClustered(orders.get(closestOrderIndex2));

            // Add closest orders to the cluster
            addClosestOrders(newCluster, orders, distances, clustered);
            clusters.add(newCluster);  // Add the new cluster to the list of clusters
        }

        // Continue with the rest of the sorted pairs
        List<int[]> sortedPairs = getSortedPairs(distances, size);

        for (int[] pair : sortedPairs) {
            if (clusters.size() >= availableTruckCount) {
                break;  // Exit the loop if no more clusters can be formed
            }

            int i = pair[0];
            int j = pair[1];

            // Check if neither of the orders in the pair is already clustered
            if (!clustered[i] && !clustered[j]) {
                // Create a new cluster
                List<Order> newCluster = new ArrayList<>();
                newCluster.add(orders.get(i));
                newCluster.add(orders.get(j));
                clustered[i] = true;
                clustered[j] = true;

                // Mark orders as clustered in the order service
                orderService.markOrderAsClustered(orders.get(i));
                orderService.markOrderAsClustered(orders.get(j));

                // Add closest orders to the cluster
                addClosestOrders(newCluster, orders, distances, clustered);
                clusters.add(newCluster);  // Add the new cluster to the list of clusters
            }
        }

        // Add remaining unclustered orders to new clusters
        for (int i = 0; i < size; i++) {
            if (clusters.size() >= availableTruckCount) {
                break;
            }

            if (!clustered[i]) {
                List<Order> newCluster = new ArrayList<>();
                newCluster.add(orders.get(i));
                clusters.add(newCluster);
                clustered[i] = true;
                orderService.markOrderAsClustered(orders.get(i));
            }
        }

        // Assign clusters to trucks
        clusterAssignmentService.assignClustersToTrucks(clusters);

        // Log details of the clusters
        logClusterDetails(clusters);

        return clusters;
    }

    // Method to add closest orders to a cluster
    private void addClosestOrders(List<Order> cluster, List<Order> orders, double[][] distances, boolean[] clustered) {
        boolean added = true;

        while (added) {
            added = false;
            double closestDistance = Double.MAX_VALUE;
            int closestOrderIndex = -1;

            // Find the closest unclustered order to the cluster
            for (Order clusterOrder : cluster) {
                int clusterOrderIndex = orders.indexOf(clusterOrder);
                for (int i = 0; i < orders.size(); i++) {
                    if (!clustered[i]) {
                        double distance = distances[clusterOrderIndex][i];
                        if (distance < closestDistance) {
                            closestDistance = distance;
                            closestOrderIndex = i;
                        }
                    }
                }
            }

            if (closestOrderIndex != -1) {
                Order closestOrder = orders.get(closestOrderIndex);
                double totalWeight = getTotalWeight(cluster) + closestOrder.getTotalWeight();
                double totalVolume = getTotalVolume(cluster) + closestOrder.getTotalVolume();

                // Check if adding the closest order exceeds weight or volume limits
                if (totalWeight <= MAX_WEIGHT && totalVolume <= MAX_VOLUME) {
                    boolean canAdd = false;
                    List<Order> potentialCluster = new ArrayList<>(cluster);
                    potentialCluster.add(closestOrder);
                    double newArea = 0;
                    if (potentialCluster.size() > 2) {
                        newArea = areaCalculatorService.calculateClusterArea(potentialCluster);
                        logger.info(String.format("Trying to add Order %d to cluster. New area would be %.2f", closestOrder.getId(), newArea));
                    }

                    // Check if adding the closest order exceeds area limit
                    if (newArea <= MAX_AREA || cluster.size() < 3) {
                        canAdd = true;
                    } else {
                        logger.info(String.format("Cannot add Order %d to cluster. Exceeds max area: %.2f > %.2f", closestOrder.getId(), newArea, MAX_AREA));
                        for (int i = 0; i < orders.size(); i++) {
                            if (!clustered[i]) {
                                potentialCluster.remove(potentialCluster.size() - 1); // Remove last added order
                                potentialCluster.add(orders.get(i));
                                newArea = areaCalculatorService.calculateClusterArea(potentialCluster);
                                logger.info(String.format("Trying to add Order %d to cluster. New area would be %.2f", orders.get(i).getId(), newArea));
                                if (newArea <= MAX_AREA) {
                                    closestOrderIndex = i;
                                    closestOrder = orders.get(i);
                                    canAdd = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (canAdd) {
                        // Check if the maximum distance constraint is met
                        boolean maxDistanceValid = true;
                        for (Order order : cluster) {
                            double distance = distanceCalculatorService.calculateDistance(order.getDeliveryLatitude(), order.getDeliveryLongitude(), closestOrder.getDeliveryLatitude(), closestOrder.getDeliveryLongitude());
                            if (distance > MAX_DISTANCE) {
                                maxDistanceValid = false;
                                break;
                            }
                        }

                        if (maxDistanceValid) {
                            cluster.add(closestOrder);
                            clustered[closestOrderIndex] = true;
                            orderService.markOrderAsClustered(closestOrder);
                            added = true;
                        }
                    }
                }
            }
        }
    }

    // Method to get sorted pairs of orders based on distances
    private List<int[]> getSortedPairs(double[][] distances, int size) {
        List<int[]> pairs = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                pairs.add(new int[]{i, j});
            }
        }

        pairs.sort(Comparator.comparingDouble(pair -> distances[pair[0]][pair[1]]));
        return pairs;
    }

    // Method to log details of the clusters
    private void logClusterDetails(List<List<Order>> clusters) {
        logger.info("Comprehensive Clustering Output:");
        for (int i = 0; i < clusters.size(); i++) {
            List<Order> cluster = clusters.get(i);
            double totalVolume = getTotalVolume(cluster);
            double totalWeight = getTotalWeight(cluster);
            double area = areaCalculatorService.calculateClusterArea(cluster);

            StringBuilder clusterDetails = new StringBuilder();
            clusterDetails.append(String.format("Cluster %d: Total Volume: %.2f, Total Weight: %.2f, Area: %.2f, Orders: ", i + 1, totalVolume, totalWeight, area));

            for (Order order : cluster) {
                clusterDetails.append(String.format("Order %d (Volume: %.2f, Weight: %.2f), ", order.getId(), order.getTotalVolume(), order.getTotalWeight()));
            }

            if (clusterDetails.length() > 0) {
                clusterDetails.setLength(clusterDetails.length() - 2);
            }

            logger.info(clusterDetails.toString());
        }
    }

    // Method to get total volume of a list of orders
    private double getTotalVolume(List<Order> orders) {
        return orders.stream().mapToDouble(Order::getTotalVolume).sum();
    }

    // Method to get total weight of a list of orders
    private double getTotalWeight(List<Order> orders) {
        return orders.stream().mapToDouble(Order::getTotalWeight).sum();
    }
}

