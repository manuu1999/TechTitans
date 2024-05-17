package com.fhnw.TechTitans.repository;

import com.fhnw.TechTitans.model.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, Integer> {
}
