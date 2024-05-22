package com.fhnw.TechTitans.service;

import com.fhnw.TechTitans.model.Order;
import com.fhnw.TechTitans.model.Truck;
import com.fhnw.TechTitans.model.OrderCluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

@Service
public class ClusteringService {
    public static final float MAX_CAPACITY_M3 = 80.0f;
    public static final float MAX_WEIGHT = 80.0f;
    public static final long OLD_ORDER_THRESHOLD = 7 * 24 * 60 * 60 * 1000L; // 7 days in milliseconds

    @Autowired
    private OrderService orderService;

    public List<OrderCluster> clusterOrders(List<Order> orders, List<Truck> trucks) {
        List<OrderCluster> clusters = new ArrayList<>();
        List<Order> remainingOrders = new ArrayList<>();

        // Filter out orders that are already in a cluster
        for (Order order : orders) {
            if (!order.isInCluster()) { // check order entity column in_cluster = false to continue
                remainingOrders.add(order);
            }
        }

        remainingOrders.sort(Comparator.comparing(Order::getTimestamp)); // Older orders first

        while (!remainingOrders.isEmpty() && !trucks.isEmpty()) {
            Truck closestTruck = findClosestTruck(remainingOrders.get(0), trucks);
            if (closestTruck == null) {
                break;
            }

            OrderCluster currentCluster = new OrderCluster(closestTruck);
            trucks.remove(closestTruck);  // Remove truck from the list as it is now assigned

            while (true) {
                Order bestOrder = findBestOrder(currentCluster, remainingOrders);
                if (bestOrder == null) {
                    break;
                }
                splitAndAddOrder(currentCluster, bestOrder, remainingOrders);
            }

            clusters.add(currentCluster);
        }

        // Mark clustered orders in db table order column in_cluster = true
        for (OrderCluster cluster : clusters) {
            for (Order order : cluster.getOrders()) {
                orderService.markOrderAsClustered(order);
            }
        }

        printOrderClusters(clusters);

        return clusters;
    }

    private Truck findClosestTruck(Order order, List<Truck> trucks) {
        Truck closestTruck = null;
        double closestDistance = Double.MAX_VALUE;

        for (Truck truck : trucks) {
            double distance = calculateDistance(order.getDeliveryLatitude(), order.getDeliveryLongitude(), truck.getLatitude(), truck.getLongitude());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestTruck = truck;
            }
        }

        return closestTruck;
    }

    private void splitAndAddOrder(OrderCluster cluster, Order order, List<Order> remainingOrders) {
        while (order.getTotalVolume() > MAX_CAPACITY_M3 || order.getTotalWeight() > MAX_WEIGHT) {
            Order partialOrder = orderService.splitOrder(order, MAX_CAPACITY_M3 - cluster.getTotalVolume(), MAX_WEIGHT - cluster.getTotalWeight());
            cluster.addOrder(partialOrder);
            orderService.markOrderAsClustered(partialOrder);
        }
        cluster.addOrder(order);
        remainingOrders.remove(order);
        orderService.saveOrder(order); // Ensure the updated order is saved
    }

    private Order findBestOrder(OrderCluster cluster, List<Order> remainingOrders) {
        PriorityQueue<Order> priorityQueue = new PriorityQueue<>(Comparator.comparingDouble(o -> calculateDistance(cluster.getCenterLatitude(), cluster.getCenterLongitude(), o.getDeliveryLatitude(), o.getDeliveryLongitude())));

        for (Order order : remainingOrders) {
            if (cluster.getTotalVolume() + order.getTotalVolume() <= MAX_CAPACITY_M3 && cluster.getTotalWeight() + order.getTotalWeight() <= MAX_WEIGHT) {
                priorityQueue.add(order);
            }
        }

        return priorityQueue.isEmpty() ? null : priorityQueue.poll();
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the Earth in kilometers
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public static void printOrderClusters(List<OrderCluster> clusters) {
        for (int i = 0; i < clusters.size(); i++) {
            OrderCluster cluster = clusters.get(i);
            System.out.println("Cluster " + (i + 1) + " (Truck " + cluster.getTruck().getId() + "):");
            for (Order order : cluster.getOrders()) {
                System.out.println("  Order ID: " + order.getId() +
                        ", Volume: " + order.getTotalVolume() +
                        ", Weight: " + order.getTotalWeight() +
                        ", Delivery Latitude: " + order.getDeliveryLatitude() +
                        ", Delivery Longitude: " + order.getDeliveryLongitude());
            }
        }
    }
}
