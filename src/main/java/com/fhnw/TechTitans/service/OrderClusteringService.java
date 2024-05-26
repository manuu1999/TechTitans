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

    private static final Logger logger = Logger.getLogger(OrderClusteringService.class.getName());

    @Autowired
    private OrderDistancesService orderDistancesService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TruckService truckService;

    @Autowired
    private ClusterAssignmentService clusterAssignmentService;

    private static final double MAX_WEIGHT = 6000.0;
    private static final double MAX_VOLUME = 40.0;

    // Inside the OrderClusteringService class

    public List<List<Order>> clusterOrders(List<Order> orders) {
        orders = orders.stream()
                .filter(order -> !order.isInCluster())
                .collect(Collectors.toList());

        if (orders.isEmpty()) {
            return new ArrayList<>();
        }

        List<Truck> availableTrucks = truckService.getAvailableTrucks();
        int availableTruckCount = availableTrucks.size();

        if (availableTruckCount == 0) {
            return new ArrayList<>();
        }

        double[][] distances = orderDistancesService.calculateAllDistances(orders);
        int size = orders.size();
        List<List<Order>> clusters = new ArrayList<>();
        boolean[] clustered = new boolean[size];

        List<int[]> sortedPairs = getSortedPairs(distances, size);

        for (int[] pair : sortedPairs) {
            if (clusters.size() >= availableTruckCount) {
                break;
            }

            int i = pair[0];
            int j = pair[1];

            if (!clustered[i] && !clustered[j]) {
                List<Order> newCluster = new ArrayList<>();
                newCluster.add(orders.get(i));
                newCluster.add(orders.get(j));
                clustered[i] = true;
                clustered[j] = true;

                orderService.markOrderAsClustered(orders.get(i));
                orderService.markOrderAsClustered(orders.get(j));

                addClosestOrders(newCluster, orders, distances, clustered);
                clusters.add(newCluster);
            }
        }

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

        clusterAssignmentService.assignClustersToTrucks(clusters);

        return clusters;
    }


    private void addClosestOrders(List<Order> cluster, List<Order> orders, double[][] distances, boolean[] clustered) {
        boolean added = true;

        while (added) {
            added = false;
            double closestDistance = Double.MAX_VALUE;
            int closestOrderIndex = -1;

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

                if (totalWeight <= MAX_WEIGHT && totalVolume <= MAX_VOLUME) {
                    cluster.add(closestOrder);
                    clustered[closestOrderIndex] = true;
                    orderService.markOrderAsClustered(closestOrder);
                    added = true;
                }
            }
        }
    }


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

    private void logClusterDetails(List<List<Order>> clusters) {
        logger.info("Comprehensive Clustering Output:");
        for (int i = 0; i < clusters.size(); i++) {
            List<Order> cluster = clusters.get(i);
            double totalVolume = getTotalVolume(cluster);
            double totalWeight = getTotalWeight(cluster);

            StringBuilder clusterDetails = new StringBuilder();
            clusterDetails.append(String.format("Cluster %d: Total Volume: %.2f, Total Weight: %.2f, Orders: ", i + 1, totalVolume, totalWeight));

            for (Order order : cluster) {
                clusterDetails.append(String.format("Order %d (Volume: %.2f, Weight: %.2f), ", order.getId(), order.getTotalVolume(), order.getTotalWeight()));
            }

            // Remove trailing comma and space
            if (clusterDetails.length() > 0) {
                clusterDetails.setLength(clusterDetails.length() - 2);
            }

            logger.info(clusterDetails.toString());
        }
    }

    private double getTotalVolume(List<Order> orders) {
        return orders.stream().mapToDouble(Order::getTotalVolume).sum();
    }

    private double getTotalWeight(List<Order> orders) {
        return orders.stream().mapToDouble(Order::getTotalWeight).sum();
    }
}
