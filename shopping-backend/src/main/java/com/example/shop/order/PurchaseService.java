package com.example.shop.order;

import com.example.shop.cart.CartRepository;
import com.example.shop.product.ProductRepository;
import com.example.shop.user.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseService {

    private final CartRepository cartRepo;
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;

    public PurchaseService(CartRepository cartRepo,
                           ProductRepository productRepo,
                           OrderRepository orderRepo) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
    }

    @Transactional
    public Order purchase(AppUser user) {
        var cartItems = cartRepo.findAllByUser(user);
        if (cartItems.isEmpty()) throw new IllegalStateException("Cart is empty");

        // Validate stock + compute totals
        long total = 0;
        for (var ci : cartItems) {
            var p = ci.getProduct();
            if (p.getStock() < ci.getQuantity()) {
                throw new IllegalStateException("Not enough stock for product: " + p.getName());
            }
            total += (long) ci.getQuantity() * p.getPrice();
        }

        // Deduct stock and create order
        Order order = new Order();
        order.setUser(user);
        order.setTotal(total);

        for (var ci : cartItems) {
            var p = ci.getProduct();
            p.setStock(p.getStock() - ci.getQuantity());
            productRepo.save(p);

            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setQuantity(ci.getQuantity());
            oi.setPriceAtPurchase(p.getPrice());
            order.addItem(oi);
        }

        Order saved = orderRepo.save(order);
        cartRepo.deleteAllByUser(user);
        return saved;
    }
}
