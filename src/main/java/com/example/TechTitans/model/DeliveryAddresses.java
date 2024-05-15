package com.example.TechTitans.model;

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
    @ManyToOne
    @JoinColumn(name = "route_route_id", referencedColumnName = "route_id")
    private Route route;

    @Id
    @Column(name = "delivery_id", nullable = false)
    private Integer deliveryId;

    @ManyToOne
    @JoinColumn(name = "customer_customer_id", nullable = false)
    private Customer customer;

    @Column(name = "delivery_address", nullable = false, length = 120)
    private String deliveryAddress;

    // Getters and setters
}

