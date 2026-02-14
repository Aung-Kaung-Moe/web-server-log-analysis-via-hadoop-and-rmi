package com.example.shop.seed;

import com.example.shop.product.Product;
import com.example.shop.product.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository products;

    public DataSeeder(ProductRepository products) {
        this.products = products;
    }

    @Override
    public void run(String... args) {
        if (products.count() > 0) return;

        products.save(make("Apple", 1000, 50));
        products.save(make("Orange", 800, 80));
        products.save(make("Coffee", 2500, 30));
    }

    private Product make(String name, long price, int stock) {
        Product p = new Product();
        p.setName(name);
        p.setPrice(price);
        p.setStock(stock);
        return p;
    }
}
