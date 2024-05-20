package com.fhnw.TechTitans.model;

import com.fhnw.TechTitans.model.Order;
import com.fhnw.TechTitans.model.Truck;

import java.util.ArrayList;
import java.util.List;

public class OrderCluster {
    private List<Order> orders = new ArrayList<>();
    private float totalVolume = 0;
    private float totalWeight = 0;

    public void addOrder(Order order) {
        orders.add(order);
        totalVolume += order.getTotalVolume();
        totalWeight += order.getTotalWeight();
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
}
