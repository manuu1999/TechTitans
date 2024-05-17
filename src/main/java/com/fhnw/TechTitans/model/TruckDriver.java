package com.fhnw.TechTitans.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "truck_driver")
public class TruckDriver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "truck_driver_id", nullable = false)
    private Integer id;

    @Column(name = "driver_name", nullable = false, length = 60)
    private String driverName;

    @Column(name = "email_address", nullable = false, length = 60)
    private String emailAddress;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "password", nullable = false, length = 40)
    private String password;

    // Getters and setters
}
