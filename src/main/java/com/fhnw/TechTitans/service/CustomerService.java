package com.fhnw.TechTitans.service;

import com.fhnw.TechTitans.model.Customer;
import com.fhnw.TechTitans.model.DeliveryAddresses;
import com.fhnw.TechTitans.repository.CustomerRepository;
import com.fhnw.TechTitans.repository.DeliveryAddressesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DeliveryAddressesRepository deliveryAddressesRepository;

    public Customer findById(Integer id) {
        return customerRepository.findById(id).orElse(null);
    }

    public DeliveryAddresses findAddressById(Integer id) {
        return deliveryAddressesRepository.findById(id).orElse(null);
    }
}
