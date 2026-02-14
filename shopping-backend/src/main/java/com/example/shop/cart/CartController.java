package com.example.shop.cart;

import com.example.shop.cart.dto.AddToCartRequest;
import com.example.shop.user.AppUser;
import com.example.shop.user.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cart;
    private final UserRepository users;

    public CartController(CartService cart, UserRepository users) {
        this.cart = cart;
        this.users = users;
    }

    private AppUser currentUser(Authentication auth) {
        String username = auth.getName();
        return users.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @GetMapping
    public List<CartItem> view(Authentication auth) {
        return cart.getCart(currentUser(auth));
    }

    @PostMapping("/items")
    public CartItem add(Authentication auth, @Valid @RequestBody AddToCartRequest req) {
        return cart.add(currentUser(auth), req);
    }

    @DeleteMapping("/items/{itemId}")
    public void remove(Authentication auth, @PathVariable long itemId) {
        cart.remove(currentUser(auth), itemId);
    }
}
