package com.fhnw.TechTitans.service;

import com.fhnw.TechTitans.model.ClusterAssignment;
import com.fhnw.TechTitans.model.Order;
import com.fhnw.TechTitans.model.Truck;
import com.fhnw.TechTitans.repository.ClusterAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class ClusterAssignmentService {

    private static final Logger logger = Logger.getLogger(ClusterAssignmentService.class.getName());

    @Autowired
    private TruckService truckService;

    @Autowired
    private ClusterAssignmentRepository clusterAssignmentRepository;

    /**
     * Assign clusters to the nearest available trucks and record the assignments.
     *
     * @param clusters List of clusters, where each cluster is a list of orders.
     */
    public void assignClustersToTrucks(List<List<Order>> clusters) {
        List<Truck> availableTrucks = truckService.getAvailableTrucks();

        if (availableTrucks.isEmpty()) {
            logger.info("No available trucks to assign to clusters.");
            return;
        }

        long clusterId = 1; // Initialize cluster ID

        for (List<Order> cluster : clusters) {
            double[] centerCoordinates = calculateClusterCenter(cluster);
            Truck nearestTruck = findNearestTruck(centerCoordinates, availableTrucks);
            if (nearestTruck != null) {
                logger.info(String.format("Assigning truck %d to cluster %d with center coordinates (%.6f, %.6f)",
                        nearestTruck.getId(), clusterId, centerCoordinates[0], centerCoordinates[1]));

                assignOrdersToCluster(clusterId++, nearestTruck, cluster);
                truckService.setTruckStatusUnavailable(nearestTruck.getId());
                availableTrucks.remove(nearestTruck); // Remove the assigned truck from the list of available trucks
            }
        }
    }

    private double[] calculateClusterCenter(List<Order> cluster) {
        double totalLatitude = 0;
        double totalLongitude = 0;

        for (Order order : cluster) {
            totalLatitude += order.getLatitude();
            totalLongitude += order.getLongitude();
        }

        return new double[]{totalLatitude / cluster.size(), totalLongitude / cluster.size()};
    }

    private Truck findNearestTruck(double[] centerCoordinates, List<Truck> availableTrucks) {
        Truck nearestTruck = null;
        double shortestDistance = Double.MAX_VALUE;

        for (Truck truck : availableTrucks) {
            double distance = DistanceCalculatorService.calculateDistance(
                    centerCoordinates[0], centerCoordinates[1],
                    truck.getLatitude(), truck.getLongitude()
            );

            if (distance < shortestDistance) {
                shortestDistance = distance;
                nearestTruck = truck;
            }
        }

        return nearestTruck;
    }

    private void assignOrdersToCluster(Long clusterId, Truck truck, List<Order> orders) {
        for (Order order : orders) {
            ClusterAssignment clusterAssignment = new ClusterAssignment();
            clusterAssignment.setClusterId(clusterId);
            clusterAssignment.setTruck(truck);
            clusterAssignment.setOrder(order);
            clusterAssignmentRepository.save(clusterAssignment);
        }
    }
}
