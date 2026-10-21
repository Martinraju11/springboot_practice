package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "categories", schema = "details")
@Data
@NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    // One Category has Many Products
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @JsonIgnore // Prevents infinite recursion when converting to JSON
    private List<Product> products;
}
