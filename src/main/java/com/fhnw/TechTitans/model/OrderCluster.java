package com.fhnw.TechTitans.model;

import java.util.ArrayList;
import java.util.List;

public class OrderCluster {
    private Truck truck;
    private List<Order> orders;
    private float totalVolume;
    private float totalWeight;
    private double centerLatitude;
    private double centerLongitude;

    public OrderCluster(Truck truck) {
        this.truck = truck;
        this.orders = new ArrayList<>();
        this.totalVolume = 0;
        this.totalWeight = 0;
        this.centerLatitude = truck.getLatitude();
        this.centerLongitude = truck.getLongitude();
    }

    public void addOrder(Order order) {
        orders.add(order);
        totalVolume += order.getTotalVolume();
        totalWeight += order.getTotalWeight();
        updateClusterCenter();
    }

    public float getTotalVolume() {
        return totalVolume;
    }

    public float getTotalWeight() {
        return totalWeight;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public Truck getTruck() {
        return truck;
    }

    public double getCenterLatitude() {
        return centerLatitude;
    }

    public double getCenterLongitude() {
        return centerLongitude;
    }

    private void updateClusterCenter() {
        double totalLat = 0;
        double totalLon = 0;
        for (Order order : orders) {
            totalLat += order.getDeliveryLatitude();
            totalLon += order.getDeliveryLongitude();
        }
        this.centerLatitude = totalLat / orders.size();
        this.centerLongitude = totalLon / orders.size();
    }
}
