package com.fhnw.TechTitans.service;

import com.fhnw.TechTitans.model.Order;
import java.util.List;

public class AreaCalculatorService {

    public double calculateClusterArea(List<Order> cluster) {
        if (cluster == null || cluster.size() < 3) {
            // Not enough points to form a polygon
            return 0;
        }

        int n = cluster.size();
        double area = 0;

        for (int i = 0; i < n; i++) {
            Order currentOrder = cluster.get(i);
            Order nextOrder = cluster.get((i + 1) % n);

            double x1 = currentOrder.getDeliveryLongitude();
            double y1 = currentOrder.getDeliveryLatitude();
            double x2 = nextOrder.getDeliveryLongitude();
            double y2 = nextOrder.getDeliveryLatitude();

            area += (x1 * y2) - (x2 * y1);
        }

        return Math.abs(area / 2.0);
    }
}
