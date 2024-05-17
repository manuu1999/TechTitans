package com.fhnw.TechTitans.repository;

import com.fhnw.TechTitans.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Customer findByEmailAddressAndPassword(String emailAddress, String password);
}
