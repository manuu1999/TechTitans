package com.fhnw.TechTitans.service;

import com.fhnw.TechTitans.model.Order;
import com.fhnw.TechTitans.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Integer id) {
        return orderRepository.findById(id);
    }

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public void deleteOrder(Integer id) {
        orderRepository.deleteById(id);
    }

    public Order splitOrder(Order order, float maxVolume, float maxWeight) {
        Order newOrder = order.split(maxVolume, maxWeight);
        saveOrder(newOrder);
        saveOrder(order);
        return newOrder;
    }

    public void markOrderAsClustered(Order order) {
        order.setInCluster(true);
        saveOrder(order);  // as soon as it gets assigned to a cluster, it should be set to true
    }

    public double calculateClusterArea(List<Order> cluster) {
        AreaCalculatorService calculator = new AreaCalculatorService();
        return calculator.calculateClusterArea(cluster);
    }

    // Method to calculate the center point of all orders
    public double[] calculateCenterPoint(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            throw new IllegalArgumentException("Order list cannot be null or empty");
        }

        double sumLatitude = 0.0;
        double sumLongitude = 0.0;
        int count = 0;

        for (Order order : orders) {
            Double latitude = order.getDeliveryLatitude();
            Double longitude = order.getDeliveryLongitude();
            if (latitude != null && longitude != null) {
                sumLatitude += latitude;
                sumLongitude += longitude;
                count++;
            }
        }

        if (count == 0) {
            throw new IllegalArgumentException("No valid order locations found");
        }

        double centerLatitude = sumLatitude / count;
        double centerLongitude = sumLongitude / count;

        return new double[]{centerLatitude, centerLongitude};
    }


    public List<Order> getOrdersByIds(List<Integer> ids) {
        return orderRepository.findAllById(ids);
    }


}
