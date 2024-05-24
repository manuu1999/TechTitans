package com.fhnw.TechTitans.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderProduct> orderProducts;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private DeliveryAddresses deliveryAddress;

    @Column(name = "in_cluster", nullable = false)
    private boolean inCluster = false;

    @Column(name = "is_clustered", nullable = false)
    private boolean isClustered = false;

    public float getTotalVolume() {
        return orderProducts.stream()
                .map(op -> op.getProduct().getSizeInM3() * op.getQuantity())
                .reduce(0f, Float::sum);
    }

    public float getTotalWeight() {
        return orderProducts.stream()
                .map(op -> op.getProduct().getGrossWeight() * op.getQuantity())
                .reduce(0f, Float::sum);
    }

    public Double getDeliveryLatitude() {
        return deliveryAddress != null ? deliveryAddress.getLatitude() : null;
    }

    public Double getDeliveryLongitude() {
        return deliveryAddress != null ? deliveryAddress.getLongitude() : null;
    }

    public Order split(float maxVolume, float maxWeight) {
        Order newOrder = new Order();
        newOrder.setCustomer(this.customer);
        newOrder.setDeliveryAddress(this.deliveryAddress);
        newOrder.setTimestamp(LocalDateTime.now()); // Ensure the split order has a new timestamp
        newOrder.setExpectedDeliveryDate(this.expectedDeliveryDate);
        List<OrderProduct> splitProducts = new ArrayList<>();
        float splitVolume = 0;
        float splitWeight = 0;

        for (OrderProduct product : new ArrayList<>(orderProducts)) {
            if (splitVolume + product.getProduct().getSizeInM3() <= maxVolume && splitWeight + product.getProduct().getGrossWeight() <= maxWeight) {
                splitProducts.add(product);
                splitVolume += product.getProduct().getSizeInM3();
                splitWeight += product.getProduct().getGrossWeight();
                orderProducts.remove(product);
            }
        }

        newOrder.setOrderProducts(splitProducts);
        for (OrderProduct op : splitProducts) {
            op.setOrder(newOrder); // Ensure orderProducts are properly set for the new order
        }

        return newOrder;
    }

    public void updateAfterSplit(Order partialOrder) {
        float remainingVolume = 0;
        float remainingWeight = 0;

        for (OrderProduct product : orderProducts) {
            remainingVolume += product.getProduct().getSizeInM3();
            remainingWeight += product.getProduct().getGrossWeight();
        }
    }

    public void setIsClustered(boolean b) {
        inCluster = b;
    }

    public double getLatitude() {
        return getDeliveryLatitude();
    }

    public double getLongitude() {
        return getDeliveryLongitude();
    }

    public float getVolume() {
        return getTotalVolume();
    }

    public float getWeight() {
        return getTotalWeight();
    }
}
