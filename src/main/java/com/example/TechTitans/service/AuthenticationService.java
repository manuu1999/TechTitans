package com.example.TechTitans.service;

import com.example.TechTitans.model.Administrator;
import com.example.TechTitans.model.Customer;
import com.example.TechTitans.model.TruckDriver;
import com.example.TechTitans.repository.AdministratorRepository;
import com.example.TechTitans.repository.CustomerRepository;
import com.example.TechTitans.repository.TruckDriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TruckDriverRepository truckDriverRepository;

    public Administrator loginAsAdministrator(String email, String password) {
        return administratorRepository.findByEmailAddressAndPassword(email, password);
    }

    public Customer loginAsCustomer(String email, String password) {
        return customerRepository.findByEmailAddressAndPassword(email, password);
    }

    public TruckDriver loginAsTruckDriver(String email, String password) {
        return truckDriverRepository.findByEmailAddressAndPassword(email, password);
    }
}
