package com.example.TechTitans.repository;

import com.example.TechTitans.model.TruckDriver;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TruckDriverRepository extends JpaRepository<TruckDriver, Integer> {
    TruckDriver findByEmailAddressAndPassword(String emailAddress, String password);
}


