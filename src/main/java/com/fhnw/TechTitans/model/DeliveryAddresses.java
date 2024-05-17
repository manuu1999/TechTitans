package com.fhnw.TechTitans.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "delivery_addresses")
public class DeliveryAddresses {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "delivery_id", nullable = false)
    private Integer deliveryId;

    @Column(name = "delivery_address", nullable = false, length = 120)
    private String deliveryAddress;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @ManyToOne
    @JoinColumn(name = "route_route_id", referencedColumnName = "route_id")
    private Route route;

    @ManyToOne
    @JoinColumn(name = "customer_customer_id", nullable = false)
    private Customer customer;
}
