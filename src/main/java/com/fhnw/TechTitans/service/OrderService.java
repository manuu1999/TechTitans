package com.fhnw.TechTitans.service;

import com.fhnw.TechTitans.model.Order;
import com.fhnw.TechTitans.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Integer id) {
        return orderRepository.findById(id);
    }

    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    public void deleteOrder(Integer id) {
        orderRepository.deleteById(id);
    }

    public Order splitOrder(Order order, float maxVolume, float maxWeight) {
        Order newOrder = order.split(maxVolume, maxWeight);
        saveOrder(newOrder);
        saveOrder(order);
        return newOrder;
    }

    public void markOrderAsClustered(Order order) {
        order.setInCluster(true);
        saveOrder(order);  // as soon as it gets assigned to a cluster, it should be set to true
    }


    public List<Order> getOrdersByIds(List<Integer> ids) {
        return orderRepository.findAllById(ids);
    }


}
