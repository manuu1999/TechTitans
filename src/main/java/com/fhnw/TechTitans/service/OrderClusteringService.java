package com.fhnw.TechTitans.service;

import com.fhnw.TechTitans.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

@Service
public class OrderClusteringService {

    private static final Logger logger = Logger.getLogger(OrderClusteringService.class.getName());

    @Autowired
    private OrderDistancesService orderDistancesService;

    private static final double MAX_WEIGHT = 80.0;
    private static final double MAX_VOLUME = 80.0;

    /**
     * Cluster orders based on the closest distances without exceeding weight and volume limits.
     *
     * @param orders List of orders to be clustered.
     * @return List of clusters, where each cluster is a list of orders.
     */
    public List<List<Order>> clusterOrders(List<Order> orders) {
        logger.info("Starting to calculate all distances between orders.");
        double[][] distances = orderDistancesService.calculateAllDistances(orders);
        int size = orders.size();
        List<List<Order>> clusters = new ArrayList<>();
        boolean[] clustered = new boolean[size];

        logger.info("Sorting all pairs of orders by distance.");
        List<int[]> sortedPairs = getSortedPairs(distances, size);

        logger.info("Forming clusters based on sorted distances and constraints.");
        for (int[] pair : sortedPairs) {
            int i = pair[0];
            int j = pair[1];

            if (!clustered[i] && !clustered[j]) {
                Order order1 = orders.get(i);
                Order order2 = orders.get(j);
                double totalWeight = order1.getTotalWeight() + order2.getTotalWeight();
                double totalVolume = order1.getTotalVolume() + order2.getTotalVolume();

                if (totalWeight <= MAX_WEIGHT && totalVolume <= MAX_VOLUME) {
                    List<Order> newCluster = new ArrayList<>();
                    newCluster.add(order1);
                    newCluster.add(order2);
                    clusters.add(newCluster);
                    clustered[i] = true;
                    clustered[j] = true;
                    logger.info(String.format("Formed new cluster with orders %d and %d. Total Weight: %.2f, Total Volume: %.2f",
                            order1.getId(), order2.getId(), totalWeight, totalVolume));
                } else {
                    logger.info(String.format("Skipping pairing orders %d and %d due to weight/volume constraints. Total Weight: %.2f, Total Volume: %.2f",
                            order1.getId(), order2.getId(), totalWeight, totalVolume));
                }
            }
        }

        logger.info("Handling remaining unclustered orders.");
        for (int i = 0; i < size; i++) {
            if (!clustered[i]) {
                List<Order> newCluster = new ArrayList<>();
                newCluster.add(orders.get(i));
                clusters.add(newCluster);
                logger.info(String.format("Formed new cluster with single order %d.", orders.get(i).getId()));
            }
        }

        logger.info("Finished clustering orders.");

        // Log the comprehensive output of clusters
        logClusterDetails(clusters);

        return clusters;
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
            float totalVolume = getTotalVolume(cluster);
            float totalWeight = getTotalWeight(cluster);

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

    private float getTotalVolume(List<Order> orders) {
        return orders.stream().map(Order::getTotalVolume).reduce(0f, Float::sum);
    }

    private float getTotalWeight(List<Order> orders) {
        return orders.stream().map(Order::getTotalWeight).reduce(0f, Float::sum);
    }
}
