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

    private static final double MAX_WEIGHT = 80.0;
    private static final double MAX_VOLUME = 80.0;

    /**
     * Cluster orders based on the closest distances without exceeding weight and volume limits.
     *
     * @param orders List of orders to be clustered.
     * @return List of clusters, where each cluster is a list of orders.
     */
    public List<List<Order>> clusterOrders(List<Order> orders) {
        logger.info("Filtering out orders that are already in a cluster.");
        orders = orders.stream()
                .filter(order -> !order.isInCluster())
                .collect(Collectors.toList());

        if (orders.isEmpty()) {
            logger.info("No orders to cluster. All orders are already in a cluster.");
            return new ArrayList<>();
        }

        List<Truck> availableTrucks = truckService.getAvailableTrucks();
        int availableTruckCount = availableTrucks.size();

        if (availableTruckCount == 0) {
            logger.info("No available trucks for clustering.");
            return new ArrayList<>();
        }

        logger.info("Starting to calculate all distances between orders.");
        double[][] distances = orderDistancesService.calculateAllDistances(orders);
        int size = orders.size();
        List<List<Order>> clusters = new ArrayList<>();
        boolean[] clustered = new boolean[size];

        logger.info("Sorting all pairs of orders by distance.");
        List<int[]> sortedPairs = getSortedPairs(distances, size);

        logger.info("Forming clusters based on sorted distances and constraints.");
        for (int[] pair : sortedPairs) {
            if (clusters.size() >= availableTruckCount) {
                logger.info("Reached the limit of available trucks. Stopping cluster formation.");
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

                truckService.setTruckStatusUnavailable(availableTrucks.get(clusters.size() - 1).getId());
            }
        }

        logger.info("Handling remaining unclustered orders.");
        for (int i = 0; i < size; i++) {
            if (clusters.size() >= availableTruckCount) {
                logger.info("Reached the limit of available trucks. Stopping cluster formation.");
                break;
            }

            if (!clustered[i]) {
                List<Order> newCluster = new ArrayList<>();
                newCluster.add(orders.get(i));
                clusters.add(newCluster);
                clustered[i] = true;
                orderService.markOrderAsClustered(orders.get(i));
                logger.info(String.format("Formed new cluster with single order %d.", orders.get(i).getId()));

                truckService.setTruckStatusUnavailable(availableTrucks.get(clusters.size() - 1).getId());
            }
        }

        logger.info("Finished clustering orders.");
        logClusterDetails(clusters);

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
                    logger.info(String.format("Added order %d to existing cluster. Total Weight: %.2f, Total Volume: %.2f",
                            closestOrder.getId(), totalWeight, totalVolume));
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
