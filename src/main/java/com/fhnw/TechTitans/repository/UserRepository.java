package com.fhnw.TechTitans.repository;

import com.fhnw.TechTitans.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    User findByEmail(String email);


}
