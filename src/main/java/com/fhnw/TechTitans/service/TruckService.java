package com.fhnw.TechTitans.service;

import com.fhnw.TechTitans.model.Truck;
import com.fhnw.TechTitans.repository.OrderRepository;
import com.fhnw.TechTitans.repository.TruckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TruckService {
    @Autowired
    private TruckRepository truckRepository;

    public List<Truck> getAllTrucks() {
        return truckRepository.findAll();
    }

    public void saveTruck(Truck truck) {
        truckRepository.save(truck);
    }
}
