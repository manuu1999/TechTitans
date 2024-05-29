package com.fhnw.TechTitans.service;

import com.fhnw.TechTitans.model.Order;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AreaCalculatorService {

    // code from following source
    // https://stackoverflow.com/questions/36022883/calculate-the-area-of-a-polygon-with-latitude-and-longitude
    // translated to java and adapted to the project

    private static final double EARTH_RADIUS = 6371.0; // Radius of the Earth in kilometers

    // Convert degrees to radians
    private static double toRadians(double degrees) {
        return degrees * Math.PI / 180.0;
    }

    // Calculate the area of a polygon given its vertices in latitude and longitude
    public double calculateClusterArea(List<Order> cluster) {
        if (cluster == null || cluster.size() < 3) {
            return 0;
        }

        double area = 0.0;
        int n = cluster.size();

        for (int i = 0; i < n; i++) {
            Order p1 = cluster.get(i);
            Order p2 = cluster.get((i + 1) % n);
            area += toRadians(p2.getDeliveryLongitude() - p1.getDeliveryLongitude()) *
                    (2 + Math.sin(toRadians(p1.getDeliveryLatitude())) + Math.sin(toRadians(p2.getDeliveryLatitude())));
        }

        area = area * EARTH_RADIUS * EARTH_RADIUS / 2.0;

        return Math.abs(area); // Area in square kilometers
    }
}
