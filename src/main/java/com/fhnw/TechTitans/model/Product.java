package com.fhnw.TechTitans.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "product")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "orderProducts"})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false)
    private Integer id;

    @Column(name = "product_name", nullable = false, length = 60)
    private String productName;

    @Column(name = "price", nullable = false)
    private Float price;

    @Column(name = "length_in_m", nullable = false)
    private Float lengthInM;

    @Column(name = "height_in_m", nullable = false)
    private Float heightInM;

    @Column(name = "depth_in_m", nullable = false)
    private Float depthInM;

    @Column(name = "size_in_m3", nullable = false)
    private Float sizeInM3;

    @Column(name = "gross_weight", nullable = false)
    private Float grossWeight;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderProduct> orderProducts;
}
