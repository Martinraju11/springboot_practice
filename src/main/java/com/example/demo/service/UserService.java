package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Business Logic: Accept UserDTO, Validate & Convert to User Entity
    public User createUser(UserDTO userDTO) {
        // Business Rule: Check for duplicate email
        if (userDTO.getEmail() != null && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Email '" + userDTO.getEmail() + "' is already registered!");
        }

        // Map DTO -> Entity
        User user = new User();
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        user.setAge(userDTO.getAge());
        user.setGender(userDTO.getGender());
        user.setIsActive(userDTO.getIsActive() != null ? userDTO.getIsActive() : true);

        return userRepository.save(user);
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // 5. Selective Update: Updates only non-null submitted fields (e.g. name, age)
    public User updateUser(Long id, UserDTO userDTO) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + id));

        // Update fields if provided
        if (userDTO.getName() != null && !userDTO.getName().trim().isEmpty()) {
            existingUser.setName(userDTO.getName().trim());
        }

        if (userDTO.getAge() != null) {
            existingUser.setAge(userDTO.getAge());
        }

        if (userDTO.getGender() != null) {
            existingUser.setGender(userDTO.getGender());
        }

        if (userDTO.getIsActive() != null) {
            existingUser.setIsActive(userDTO.getIsActive());
        }

        // Save updated existing record back to PostgreSQL
        return userRepository.save(existingUser);
    }
}
