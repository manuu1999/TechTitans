package com.fhnw.TechTitans.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "administrator")
public class Administrator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "administrator_id", nullable = false)
    private Integer id;

    @Column(name = "email_address", nullable = false, length = 100)
    private String emailAddress;

    @Column(name = "password", length = 40)
    private String password;


}
