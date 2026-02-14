package com.example.shop.order;

import com.example.shop.user.AppUser;
import com.example.shop.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final UserRepository users;

    public PurchaseController(PurchaseService purchaseService, UserRepository users) {
        this.purchaseService = purchaseService;
        this.users = users;
    }

    private AppUser currentUser(Authentication auth) {
        return users.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @PostMapping("/purchase")
    public Order purchase(Authentication auth) {
        return purchaseService.purchase(currentUser(auth));
    }
}
