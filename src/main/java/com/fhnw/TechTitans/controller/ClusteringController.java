package com.fhnw.TechTitans.controller;

import com.fhnw.TechTitans.model.Order;
import com.fhnw.TechTitans.model.Truck;
import com.fhnw.TechTitans.service.ClusteringService;
import com.fhnw.TechTitans.service.OrderService;
import com.fhnw.TechTitans.service.TruckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ClusteringController {

    @Autowired
    private ClusteringService clusteringService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private TruckService truckService;

    @GetMapping("/manageSite/clusterOrders")
    public void clusterOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<Truck> trucks = truckService.getAllTrucks();
        clusteringService.clusterOrders(orders, trucks);
    }
}
