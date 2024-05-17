package com.fhnw.TechTitans.repository;

import com.fhnw.TechTitans.model.TruckDriver;
import org.springframework.data.jpa.repository.JpaRepository;
public interface TruckDriverRepository extends JpaRepository<TruckDriver, Integer> {
    TruckDriver findByEmailAddressAndPassword(String emailAddress, String password);
}


