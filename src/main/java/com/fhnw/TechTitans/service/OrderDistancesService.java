package com.fhnw.TechTitans.service;

import com.fhnw.TechTitans.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.fhnw.TechTitans.service.DistanceCalculatorService.calculateDistance;

@Service
public class OrderDistancesService {

    private static final Logger logger = Logger.getLogger(OrderDistancesService.class.getName());

    @Autowired
    private TruckService truckService;

    @Autowired
    private OrderService orderService;

    /**
     * Calculate all distances between a list of orders.
     *
     * @param orders List of orders.
     * @return 2D array of distances.
     */
    public double[][] calculateAllDistances(List<Order> orders) {
        int size = orders.size();
        double[][] distances = new double[size][size];
        List<String> distanceLogs = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            Order order1 = orders.get(i);
            for (int j = 0; j < size; j++) {
                if (i == j) {
                    distances[i][j] = 0.0; // No need to calculate distance to itself
                } else if (j > i) {
                    Order order2 = orders.get(j);
                    double distance = calculateDistance(
                            order1.getLatitude(),
                            order1.getLongitude(),
                            order2.getLatitude(),
                            order2.getLongitude()
                    );
                    distances[i][j] = distance; // Distance from order i to order j
                    distances[j][i] = distance; // Distance from order j to order i (same as above)
                    distanceLogs.add(String.format(
                            "Distance between order %d (Weight: %.2f, Volume: %.2f) and order %d (Weight: %.2f, Volume: %.2f): %.2f km",
                            order1.getId(), order1.getTotalWeight(), order1.getTotalVolume(),
                            order2.getId(), order2.getTotalWeight(), order2.getTotalVolume(),
                            distance
                    ));
                }
            }
        }

        // Log all stored distance logs
        logger.info("Distance matrix:");
        for (String log : distanceLogs) {
            logger.info(log);
        }

        return distances;
    }

    /**
     * Find the order that is furthest from the center point.
     *
     * @param orders List of orders.
     * @return The order that is furthest from the center point.
     */
    public Order findFurthestOrderFromCenter(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            throw new IllegalArgumentException("Order list cannot be null or empty");
        }

        // Calculate the center point of the orders
        double[] centerPoint = orderService.calculateCenterPoint(orders);
        double centerLatitude = centerPoint[0];
        double centerLongitude = centerPoint[1];

        Order furthestOrder = null;
        double maxDistance = -1;

        // Find the order furthest from the center point
        for (Order order : orders) {
            Double latitude = order.getDeliveryLatitude();
            Double longitude = order.getDeliveryLongitude();
            if (latitude != null && longitude != null) {
                double distance = calculateDistance(centerLatitude, centerLongitude, latitude, longitude);
                if (distance > maxDistance) {
                    maxDistance = distance;
                    furthestOrder = order;
                }
            }
        }

        if (furthestOrder == null) {
            throw new IllegalArgumentException("No valid order locations found");
        }

        logger.info(String.format("Furthest order is Order %d at distance %.2f km", furthestOrder.getId(), maxDistance));
        return furthestOrder;
    }
}
