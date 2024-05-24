package com.fhnw.TechTitans.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "order_cluster")
public class OrderCluster {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_cluster_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_id", nullable = false)
    private Truck truck;

    @ManyToMany
    @JoinTable(
            name = "order_cluster_orders",
            joinColumns = @JoinColumn(name = "order_cluster_id"),
            inverseJoinColumns = @JoinColumn(name = "order_id")
    )
    private List<Order> orders = new ArrayList<>();


    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id", nullable = false)
    private Depot depot;

    @Column(name = "total_volume")
    private float totalVolume;

    @Column(name = "total_weight")
    private float totalWeight;

    @Column(name = "center_latitude")
    private double centerLatitude;

    @Column(name = "center_longitude")
    private double centerLongitude;

    @Column(name = "in_cluster")
    private boolean inCluster;

    public OrderCluster(Truck newTruck) {
        this.truck = newTruck;
        this.totalVolume = 0;
        this.totalWeight = 0;
        this.centerLatitude = newTruck.getLatitude();
        this.centerLongitude = newTruck.getLongitude();
    }

    public void addOrder(Order order) {
        orders.add(order);
        totalVolume += order.getTotalVolume();
        totalWeight += order.getTotalWeight();
        updateClusterCenter();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderCluster that = (OrderCluster) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public void setDepot(Depot depot) {
        this.depot = depot;
    }

    public void setInCluster(boolean b) {
        this.inCluster = b;
    }
}
