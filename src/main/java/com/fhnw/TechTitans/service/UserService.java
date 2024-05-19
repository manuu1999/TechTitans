package com.fhnw.TechTitans.service;

import com.fhnw.TechTitans.model.User;
import com.fhnw.TechTitans.model.Role;
import com.fhnw.TechTitans.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>());
    }

    public void saveUser(User user, PasswordEncoder passwordEncoder) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.USER); // Set default role to USER
        userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean userExists(String username, String email) {
        return userRepository.findByUsername(username) != null || userRepository.findByEmail(email) != null;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void updateUserRole(Long userId, String role) {
        //TODO: Check if the user is the same as the one currently logged in
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + userId));
        user.setRole(Role.valueOf(role));
        userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
