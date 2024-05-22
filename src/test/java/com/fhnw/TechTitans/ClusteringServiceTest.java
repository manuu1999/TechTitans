package com.fhnw.TechTitans;

import com.fhnw.TechTitans.model.*;
import com.fhnw.TechTitans.repository.OrderRepository;
import com.fhnw.TechTitans.repository.TruckRepository;
import com.fhnw.TechTitans.service.ClusteringService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ClusteringServiceTest {

    @Autowired
    private ClusteringService clusteringService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private TruckRepository truckRepository;

    @Before
    public void setUp() {
    }

    @Test
    public void testClusterOrdersWithSufficientTrucks() {
        List<Order> orders = orderRepository.findAll();
        List<Truck> trucks = truckRepository.findAll();

        List<OrderCluster> clusters = clusteringService.clusterOrders(orders, trucks);

        assertEquals(2, clusters.size());
        assertTrue(clusters.get(0).getTotalVolume() <= ClusteringService.MAX_CAPACITY_M3);
        assertTrue(clusters.get(0).getTotalWeight() <= ClusteringService.MAX_WEIGHT);
    }

    @Test(expected = RuntimeException.class)
    public void testClusterOrdersWithInsufficientTrucks() {
        List<Order> orders = orderRepository.findAll();
        List<Truck> trucks = truckRepository.findAll();

        clusteringService.clusterOrders(orders, trucks);
    }

    @Test
    public void testCalculateDistance() {
        double distance = clusteringService.calculateDistance(47.3768866, 8.541694, 48.8566, 2.3522);
        assertTrue(distance > 0);
    }
}
