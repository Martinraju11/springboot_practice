package com.example.demo.repository;

import com.example.demo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Case-insensitive search: WHERE LOWER(product) LIKE LOWER('%name%')
    List<Product> findByProductContainingIgnoreCase(String name);
}
