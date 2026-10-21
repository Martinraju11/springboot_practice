package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table(name = "products", schema = "details")
@Data 
@NoArgsConstructor 
public class Product {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @Column(name = "product")
    private String product;

    @Column(name = "price")
    private Integer price;

    // Foreign Key Relationship: Many Products belong to One Category
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private Category category;
}
