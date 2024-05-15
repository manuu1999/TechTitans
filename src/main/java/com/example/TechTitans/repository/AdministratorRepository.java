package com.example.TechTitans.repository;


import com.example.TechTitans.model.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministratorRepository extends JpaRepository<Administrator, Integer> {
    Administrator findByEmailAddressAndPassword(String emailAddress, String password);
}
