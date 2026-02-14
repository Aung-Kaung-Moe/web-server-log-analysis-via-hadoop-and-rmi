package com.example.shop.cart.dto;

import jakarta.validation.constraints.Min;

public class AddToCartRequest {
    public long productId;

    @Min(1)
    public int quantity;
}
