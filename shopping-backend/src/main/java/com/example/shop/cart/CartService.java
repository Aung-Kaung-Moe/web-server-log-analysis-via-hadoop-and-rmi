package com.example.shop.cart;

import com.example.shop.cart.dto.AddToCartRequest;
import com.example.shop.product.ProductRepository;
import com.example.shop.user.AppUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepo;
    private final ProductRepository productRepo;

    public CartService(CartRepository cartRepo, ProductRepository productRepo) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
    }

    public List<CartItem> getCart(AppUser user) {
        return cartRepo.findAllByUser(user);
    }

    @Transactional
    public CartItem add(AppUser user, AddToCartRequest req) {
        var product = productRepo.findById(req.productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (req.quantity <= 0) throw new IllegalArgumentException("quantity must be >= 1");

        var existing = cartRepo.findByUserIdAndProductId(user.getId(), product.getId()).orElse(null);
        if (existing == null) {
            CartItem ci = new CartItem();
            ci.setUser(user);
            ci.setProduct(product);
            ci.setQuantity(req.quantity);
            return cartRepo.save(ci);
        } else {
            existing.setQuantity(existing.getQuantity() + req.quantity);
            return cartRepo.save(existing);
        }
    }

    public void remove(AppUser user, long itemId) {
        CartItem ci = cartRepo.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));

        if (!ci.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Not your cart item");
        }
        cartRepo.delete(ci);
    }

    public void clear(AppUser user) {
        cartRepo.deleteAllByUser(user);
    }
}
