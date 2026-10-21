package com.example.demo.repository;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    // List<User> findByManagerId(Long managerId);
    List<User> findByRole(Role role);
    @Query("SELECT u FROM User u WHERE u.manager.id = :managerId")
    List<User> findByManagerId(@Param("managerId") Long managerId);
}