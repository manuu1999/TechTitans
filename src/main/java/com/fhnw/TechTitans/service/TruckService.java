package com.fhnw.TechTitans.service;

import com.fhnw.TechTitans.model.Truck;
import com.fhnw.TechTitans.repository.OrderRepository;
import com.fhnw.TechTitans.repository.TruckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TruckService {
    @Autowired
    private TruckRepository truckRepository;

    public List<Truck> getAllTrucks() {
        return truckRepository.findAll();
    }

    public List<Truck> getAvailableTrucks() {
        return truckRepository.findAll()
                .stream()
                .filter(truck -> "AVAILABLE".equals(truck.getStatus()))
                .collect(Collectors.toList());
    }

    public void setTruckStatusUnavailable(Integer truckId) {
        Truck truck = truckRepository.findById(truckId).orElseThrow(() -> new IllegalArgumentException("Truck not found"));
        truck.setStatus("UNAVAILABLE");
        truckRepository.save(truck);
    }


    public void saveTruck(Truck truck) {
        truckRepository.save(truck);
    }
}
