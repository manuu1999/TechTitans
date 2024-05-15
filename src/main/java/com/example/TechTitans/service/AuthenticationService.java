package com.example.TechTitans.service;


import com.example.TechTitans.model.Administrator;
import com.example.TechTitans.repository.AdministratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    private AdministratorRepository administratorRepository;

    public Administrator login(String email, String password) {
        return administratorRepository.findByEmailAddressAndPassword(email, password);
    }
}
