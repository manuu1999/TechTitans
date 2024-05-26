package com.fhnw.TechTitans.service;

import com.fhnw.TechTitans.model.Order;
import com.fhnw.TechTitans.model.Truck;

import java.util.List;

public class ClusterDTO {
    private Long clusterId;
    private List<Order> orders;
    private Truck truck;

    public ClusterDTO(Long clusterId, List<Order> orders, Truck truck) {
        this.clusterId = clusterId;
        this.orders = orders;
        this.truck = truck;
    }

    // Getters and setters
    public Long getClusterId() {
        return clusterId;
    }

    public void setClusterId(Long clusterId) {
        this.clusterId = clusterId;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public Truck getTruck() {
        return truck;
    }

    public void setTruck(Truck truck) {
        this.truck = truck;
    }
}
