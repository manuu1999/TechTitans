package com.fhnw.TechTitans.repository;

import com.fhnw.TechTitans.model.OrderCluster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderClusterRepository extends JpaRepository<OrderCluster, Integer> {

}
