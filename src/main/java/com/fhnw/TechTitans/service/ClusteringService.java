package com.fhnw.TechTitans.service;

import com.fhnw.TechTitans.model.*;
import com.fhnw.TechTitans.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import static com.fhnw.TechTitans.controller.ClusteringController.MAX_CAPACITY_M3;
import static com.fhnw.TechTitans.controller.ClusteringController.MAX_WEIGHT;

@Service
public class ClusteringService {

    @Autowired
    private OrderClusterRepository orderClusterRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DepotRepository depotRepository;

    @Autowired
    private TruckRepository truckRepository;

    public void clusterOrders() {
        List<Order> orders = orderRepository.findAll();
        List<Depot> depots = depotRepository.findAll();

        for (Order order : orders) {
            if (!order.isClustered()) {
                List<Order> cluster = new ArrayList<>();
                cluster.add(order);

                float totalVolume = order.getTotalVolume();
                float totalWeight = order.getTotalWeight();

                for (Order otherOrder : orders) {
                    if (!otherOrder.isClustered() && order != otherOrder) {
                        float otherVolume = otherOrder.getTotalVolume();
                        float otherWeight = otherOrder.getTotalWeight();

                        if ((totalVolume + otherVolume <= 150.0) && (totalWeight + otherWeight <= 150.0)) {
                            cluster.add(otherOrder);
                            totalVolume += otherVolume;
                            totalWeight += otherWeight;
                        }
                    }
                }

                Depot closestDepot = findClosestDepot(depots, order);
                Truck assignedTruck = assignTruckToCluster(closestDepot, totalVolume, totalWeight);

                if (assignedTruck != null) {
                    for (Order clusteredOrder : cluster) {
                        clusteredOrder.setInCluster(true);
                        //clusteredOrder.setClustered(true);
                        orderRepository.save(clusteredOrder);
                    }

                    // Create and save the OrderCluster entity
                    OrderCluster orderCluster = new OrderCluster();
                    orderCluster.setOrders(cluster);
                    orderCluster.setTruck(assignedTruck);
                    orderClusterRepository.save(orderCluster);
                }
            }
        }
    }

    private Depot findClosestDepot(List<Depot> depots, Order order) {
        Depot closestDepot = null;
        double minDistance = Double.MAX_VALUE;

        for (Depot depot : depots) {
            double distance = calculateDistance(order.getDeliveryLatitude(), order.getDeliveryLongitude(), depot.getLatitude(), depot.getLongitude());
            if (distance < minDistance) {
                minDistance = distance;
                closestDepot = depot;
            }
        }

        return closestDepot;
    }

    private Truck assignTruckToCluster(Depot depot, float totalVolume, float totalWeight) {
        for (Truck truck : depot.getTrucks()) {
            if (truck.getSizeCapacityInM3() >= totalVolume && truck.getWeightCapacity() >= totalWeight && truck.getStatus().equals("available")) {
                truck.setStatus("assigned");
                truckRepository.save(truck);
                return truck;
            }
        }
        return null;
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
        return R * c * 1000; // Convert to meters
    }

    public List<OrderCluster> clusterOrders(List<Order> orders, List<Truck> trucks) {
        List<OrderCluster> clusters = new ArrayList<>();
        PriorityQueue<Order> orderQueue = new PriorityQueue<>(Comparator.comparing(Order::getTotalVolume).reversed());

        for (Order order : orders) {
            orderQueue.add(order);
        }

        for (Truck truck : trucks) {
            OrderCluster cluster = new OrderCluster();
            cluster.setTruck(truck);

            float totalVolume = 0;
            float totalWeight = 0;

            while (!orderQueue.isEmpty()) {
                Order order = orderQueue.poll();
                if (totalVolume + order.getTotalVolume() <= MAX_CAPACITY_M3 && totalWeight + order.getTotalWeight() <= MAX_WEIGHT) {
                    cluster.addOrder(order);
                    totalVolume += order.getTotalVolume();
                    totalWeight += order.getTotalWeight();
                } else {
                    orderQueue.add(order);
                    break;
                }
            }

            clusters.add(cluster);
        }

        return clusters;
    }
}
