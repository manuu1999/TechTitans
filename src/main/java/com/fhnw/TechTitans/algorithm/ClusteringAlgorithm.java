package com.fhnw.TechTitans.algorithm;

import com.fhnw.TechTitans.model.Order;
import com.fhnw.TechTitans.model.Product;
import com.fhnw.TechTitans.model.Truck;

import java.util.*;

public class ClusteringAlgorithm {

    // List<Order> orders: A list of orders to be clustered.
    // List<Truck> trucks: A list of trucks used to determine clustering constraints.
    public Map<Integer, List<Order>> clusterOrders(List<Order> orders, List<Truck> trucks) {
        Map<Integer, List<Order>> clusters = new HashMap<>();
        // Returns a Map<Integer, List<Order>> where the key is an integer representing
        // the cluster ID and the value is a list of orders in that cluster.
        for (Order order : orders) {
            // Iterate through each order
            System.out.println(order);
            boolean added = false;
            for (Map.Entry<Integer, List<Order>> entry : clusters.entrySet()) {
                // Iterate through each cluster (represented by entry in the clusters map).
                List<Order> cluster = entry.getValue();
                if (canAddOrderToCluster(cluster, order, trucks.get(entry.getKey()))) {
                    cluster.add(order);
                    added = true;
                    break;
                }
            }
            if (!added) {
                List<Order> newCluster = new ArrayList<>();
                newCluster.add(order);
                clusters.put(clusters.size(), newCluster);
            }
        }
        return clusters;
    }

    private boolean canAddOrderToCluster(List<Order> cluster, Order order, Truck truck) {
        double totalVolume = cluster.stream()
                .flatMap(o -> o.getProducts().stream())
                .mapToDouble(Product::getSizeInM3)
                .sum() + order.getProducts().stream().mapToDouble(Product::getSizeInM3).sum();

        double totalWeight = cluster.stream()
                .flatMap(o -> o.getProducts().stream())
                .mapToDouble(Product::getGrossWeight)
                .sum() + order.getProducts().stream().mapToDouble(Product::getGrossWeight).sum();

        return totalVolume <= truck.getSizeCapacityInM3() && totalWeight <= truck.getWeightCapacity();
    }
}
