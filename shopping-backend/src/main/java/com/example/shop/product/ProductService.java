package com.example.shop.product;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public List<Product> list() {
        return repo.findAll();
    }

    public Product mustGet(long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));
    }
}
