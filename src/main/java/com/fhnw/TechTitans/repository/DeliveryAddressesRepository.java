package com.fhnw.TechTitans.repository;

import com.fhnw.TechTitans.model.DeliveryAddresses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryAddressesRepository extends JpaRepository<DeliveryAddresses, Integer> {
}
