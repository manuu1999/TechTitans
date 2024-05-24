package com.fhnw.TechTitans.repository;

import com.fhnw.TechTitans.model.Depot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepotRepository extends JpaRepository<Depot, Integer> {

    List<Depot> findAll();
}
