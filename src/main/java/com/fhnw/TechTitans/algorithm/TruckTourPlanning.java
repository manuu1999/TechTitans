package com.fhnw.TechTitans.algorithm;

import com.fhnw.TechTitans.model.*;
import com.fhnw.TechTitans.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TruckTourPlanning {

    @Autowired
    private TruckRepository truckRepository;

    @Autowired
    private DepotRepository depotRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private ClusteringAlgorithm clusteringAlgorithm;

    @Autowired
    private RouteOptimizationAlgorithm routeOptimizationAlgorithm;

    public void loadDataAndPlanRoutes() {
        List<Truck> trucks = truckRepository.findAll();
        List<Order> orders = orderRepository.findAll();
        List<Depot> depots = depotRepository.findAll();

        Map<Integer, List<Order>> clusters = clusteringAlgorithm.clusterOrders(orders, trucks);
        Map<Integer, Route> routes = new HashMap<>();

        for (Map.Entry<Integer, List<Order>> entry : clusters.entrySet()) {
            Depot startDepot = findNearestDepot(entry.getValue().get(0).getCustomer().getLatitude(), entry.getValue().get(0).getCustomer().getLongitude(), depots);
            Depot endDepot = determineEndDepot(startDepot, depots);
            Truck assignedTruck = trucks.get(entry.getKey());
            Route route = routeOptimizationAlgorithm.optimizeRoute(startDepot, endDepot, assignedTruck, entry.getValue());
            routes.put(route.getId(), route);
        }

        routes.values().forEach(routeRepository::save);

        outputRoutes(routes, trucks, depots);
    }

    private Depot findNearestDepot(double lat, double lon, List<Depot> depots) {
        // Implement nearest depot logic
        return depots.get(0); // Placeholder
    }

    private Depot determineEndDepot(Depot startDepot, List<Depot> depots) {
        // Implement end depot determination logic
        return depots.get(0); // Placeholder
    }

    private void outputRoutes(Map<Integer, Route> routes, List<Truck> trucks, List<Depot> depots) {
        for (Route route : routes.values()) {
            System.out.println("Cluster: " + route.getId());
            System.out.println("Truck: " + trucks.get(route.getId()).getId());
            System.out.println("Depot: " + depots.get(route.getId()).getName());
            route.getStops().forEach(stop -> System.out.println("Address: " + stop.getDeliveryAddress().getAddress()));
        }
    }
}
