package com.fhnw.TechTitans.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "route")
public class Route {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "route_id", nullable = false)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "start_depot_id", nullable = false)
    private Depot startDepot;

    @ManyToOne
    @JoinColumn(name = "end_depot_id", nullable = false)
    private Depot endDepot;

    @ManyToOne
    @JoinColumn(name = "truck_truck_id", nullable = false)
    private Truck truck;

    @ManyToOne
    @JoinColumn(name = "administrator_administrator_id", nullable = false)
    private Administrator administrator;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "total_delivery_time_in_days")
    private Integer totalDeliveryTimeInDays;

    @Column(name = "total_distance_in_km")
    private Float totalDistanceInKm;

    // Getters and setters
}
