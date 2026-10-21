package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController 
@RequestMapping("/api/products")
public class ProductController {

    @Autowired 
    private ProductService productService;

    @GetMapping 
    public List<Product> getAllProducts(){
        return productService.getAllProducts();
    }

    // GET /api/products/search?name=Lap
    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam(required = false) String name) {
        return productService.searchProducts(name);
    }

    @PostMapping 
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product savedProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }
}
