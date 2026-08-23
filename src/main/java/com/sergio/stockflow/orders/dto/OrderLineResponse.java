package com.sergio.stockflow.orders.dto;

import lombok.Getter;

import java.math.BigDecimal;

public record OrderLineResponse(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
