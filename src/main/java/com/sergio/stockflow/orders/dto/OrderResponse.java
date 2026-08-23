package com.sergio.stockflow.orders.dto;

import com.sergio.stockflow.orders.enums.OrderStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        OrderStatus status,
        BigDecimal total,
        List<OrderLineResponse> lines,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
