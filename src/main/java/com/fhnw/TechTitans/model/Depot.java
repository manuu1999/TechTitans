package com.fhnw.TechTitans.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "depot")
public class Depot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "depot_id", nullable = false)
    private Integer id;

    @Column(name = "depot_address", nullable = false, length = 100)
    private String depotAddress;

    @Column(name = "size_capacity_in_m3", nullable = false)
    private Float sizeCapacityInM3;

    @Column(name = "weight_capacity_in_kg", nullable = false)
    private Float weightCapacityInKg;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;
}
