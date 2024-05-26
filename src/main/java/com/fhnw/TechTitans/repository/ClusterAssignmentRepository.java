package com.fhnw.TechTitans.repository;

import com.fhnw.TechTitans.model.ClusterAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClusterAssignmentRepository extends JpaRepository<ClusterAssignment, Long> {
    List<ClusterAssignment> findAll();
}
