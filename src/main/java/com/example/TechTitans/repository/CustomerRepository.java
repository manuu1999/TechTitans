package com.example.TechTitans.repository;

import com.example.TechTitans.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Customer findByEmailAddressAndPassword(String emailAddress, String password);
}
