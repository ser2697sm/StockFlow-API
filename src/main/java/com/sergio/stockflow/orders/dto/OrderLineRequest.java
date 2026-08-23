package com.sergio.stockflow.orders.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderLineRequest(
        @NotNull(message = "El identificador del producto es obligatorio")
        Long productId,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad debe ser mayor o igual que 1")
        Integer quantity
) {
}
