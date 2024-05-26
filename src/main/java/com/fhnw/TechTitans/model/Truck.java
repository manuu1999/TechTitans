package com.fhnw.TechTitans.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "truck")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "depot", "user"})
public class Truck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "truck_id", nullable = false)
    private Integer id;

    @Column(name = "size_capacity_in_m3", nullable = false)
    private Float sizeCapacityInM3;

    @Column(name = "weight_capacity", nullable = false)
    private Float weightCapacity;

    @Column(name = "current_location", nullable = false, length = 100)
    private String currentLocation;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "depot_id", nullable = false)
    private Depot depot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    public boolean isAvailable() {
        return Objects.equals(status, "AVAILABLE");
    }

    public void setAvailable(boolean b) {
        status = b ? "AVAILABLE" : "UNAVAILABLE";
    }
}
