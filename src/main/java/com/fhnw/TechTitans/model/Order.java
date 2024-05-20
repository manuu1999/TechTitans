package com.fhnw.TechTitans.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "\"Order\"")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false)
    private Integer id;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "customer_customer_id", nullable = false)
    private Customer customer;

    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate;

    @OneToMany(mappedBy = "order")
    private List<OrderProduct> orderProducts;

    @OneToOne
    @JoinColumn(name = "delivery_id", nullable = false)
    private DeliveryAddresses deliveryAddress;

    // Method to calculate the total volume of the order
    public float getTotalVolume() {
        float totalVolume = 0;
        for (OrderProduct orderProduct : orderProducts) {
            totalVolume += orderProduct.getProduct().getSizeInM3() * orderProduct.getQuantity();
        }
        return totalVolume;
    }

    // Method to calculate the total weight of the order
    public float getTotalWeight() {
        float totalWeight = 0;
        for (OrderProduct orderProduct : orderProducts) {
            totalWeight += orderProduct.getProduct().getGrossWeight() * orderProduct.getQuantity();
        }
        return totalWeight;
    }

    // Method to get the latitude of the delivery address
    public Double getDeliveryLatitude() {
        return deliveryAddress != null ? deliveryAddress.getLatitude() : null;
    }

    // Method to get the longitude of the delivery address
    public Double getDeliveryLongitude() {
        return deliveryAddress != null ? deliveryAddress.getLongitude() : null;
    }
}
