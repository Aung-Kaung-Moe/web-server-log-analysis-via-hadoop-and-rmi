package com.example.shop.cart;

import com.example.shop.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findAllByUser(AppUser user);
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);
    void deleteAllByUser(AppUser user);
}
