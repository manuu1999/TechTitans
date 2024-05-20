package com.fhnw.TechTitans.service;

import com.fhnw.TechTitans.model.Order;
import com.fhnw.TechTitans.model.Truck;
import com.fhnw.TechTitans.model.OrderCluster;

import java.util.ArrayList;
import java.util.List;

public class ClusteringService {
    public List<OrderCluster> clusterOrders(List<Order> orders, List<Truck> trucks) {
        List<OrderCluster> clusters = new ArrayList<>();
        OrderCluster currentCluster = new OrderCluster();
        Truck currentTruck = trucks.get(0); // Assume at least one truck available

        for (Order order : orders) {
            float orderVolume = order.getTotalVolume();
            float orderWeight = order.getTotalWeight();

            // Check if adding this order exceeds truck capacity
            if (currentCluster.getTotalVolume() + orderVolume > currentTruck.getSizeCapacityInM3() ||
                    currentCluster.getTotalWeight() + orderWeight > currentTruck.getWeightCapacity()) {

                // Start a new cluster
                clusters.add(currentCluster);
                currentCluster = new OrderCluster();

                // Move to the next truck if available
                int nextTruckIndex = clusters.size();
                if (nextTruckIndex < trucks.size()) {
                    currentTruck = trucks.get(nextTruckIndex);
                } else {
                    // No more trucks available, handle as needed (e.g., throw exception)
                    throw new RuntimeException("Not enough trucks to handle all orders");
                }
            }

            currentCluster.addOrder(order);
        }

        // Add the last cluster if it contains any orders
        if (!currentCluster.getOrders().isEmpty()) {
            clusters.add(currentCluster);
        }

        return clusters;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Haversine formula to calculate distance between two coordinates
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in km
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
