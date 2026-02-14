package com.example.shop.product;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService products;

    public ProductController(ProductService products) {
        this.products = products;
    }

    @GetMapping
    public List<Product> list() {
        return products.list();
    }

    @GetMapping("/{id}")
    public Product get(@PathVariable long id) {
        return products.mustGet(id);
    }
}
