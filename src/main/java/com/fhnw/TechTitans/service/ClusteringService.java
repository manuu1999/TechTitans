package com.fhnw.TechTitans.service;

import com.fhnw.TechTitans.model.Order;
import com.fhnw.TechTitans.model.Truck;
import com.fhnw.TechTitans.model.OrderCluster;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class ClusteringService {
    private static final float MAX_CAPACITY_M3 = 80.0f;
    private static final float MAX_WEIGHT = 80.0f;

    public List<OrderCluster> clusterOrders(List<Order> orders, List<Truck> trucks) {
        List<OrderCluster> clusters = new ArrayList<>();
        List<Order> remainingOrders = new ArrayList<>(orders);
        Random random = new Random();
        int currentTruckIndex = 0;

        while (!remainingOrders.isEmpty()) {
            if (currentTruckIndex >= trucks.size()) {
                throw new RuntimeException("Not enough trucks to handle all orders");
            }

            OrderCluster currentCluster = new OrderCluster();
            Order currentOrder = remainingOrders.remove(random.nextInt(remainingOrders.size()));
            currentCluster.addOrder(currentOrder);

            boolean addedOrder;
            do {
                addedOrder = false;
                Order closestOrder = findClosestOrder(currentOrder, remainingOrders);
                if (closestOrder != null &&
                        currentCluster.getTotalVolume() + closestOrder.getTotalVolume() <= MAX_CAPACITY_M3 &&
                        currentCluster.getTotalWeight() + closestOrder.getTotalWeight() <= MAX_WEIGHT) {
                    currentCluster.addOrder(closestOrder);
                    remainingOrders.remove(closestOrder);
                    currentOrder = closestOrder;
                    addedOrder = true;
                }
            } while (addedOrder);

            clusters.add(currentCluster);
            currentTruckIndex++;
        }

        return clusters;
    }

    // Haversine formula to calculate distance between two coordinates
    // Source: https://gist.github.com/vananth22/888ed9a22105670e7a4092bdcf0d72e4

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                + Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private Order findClosestOrder(Order currentOrder, List<Order> remainingOrders) {
        Order closestOrder = null;
        double closestDistance = Double.MAX_VALUE;

        double currentLat = currentOrder.getDeliveryLatitude();
        double currentLon = currentOrder.getDeliveryLongitude();

        for (Order order : remainingOrders) {
            double distance = calculateDistance(currentLat, currentLon,
                    order.getDeliveryLatitude(), order.getDeliveryLongitude());

            if (distance < closestDistance) {
                closestDistance = distance;
                closestOrder = order;
            }
        }

        return closestOrder;
    }
}
