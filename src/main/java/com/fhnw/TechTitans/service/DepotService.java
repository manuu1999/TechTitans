package com.fhnw.TechTitans.service;

import com.fhnw.TechTitans.model.Depot;
import com.fhnw.TechTitans.model.Order;
import com.fhnw.TechTitans.repository.DepotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepotService {

    @Autowired
    private DepotRepository depotRepository;

    public List<Depot> getAllDepots() {
        return depotRepository.findAll();
    }

    public Depot findNearestDepotWithProduct(Order order, List<Depot> depots) {
        Depot nearestDepot = null;
        double minDistance = Double.MAX_VALUE;

        for (Depot depot : depots) {
            // Check if the depot has the required product
            boolean hasProduct = checkProductAvailability(depot, order);
            if (hasProduct) {
                double distance = calculateDistance(order.getDeliveryLatitude(), order.getDeliveryLongitude(), depot.getLatitude(), depot.getLongitude());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestDepot = depot;
                }
            }
        }
        return nearestDepot;
    }

    private boolean checkProductAvailability(Depot depot, Order order) {
        // Implement logic to check if the depot has the required product
        // Placeholder implementation
        return true;
    }

    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // Convert to km
        return distance;
    }
}
