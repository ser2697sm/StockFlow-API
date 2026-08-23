package com.sergio.stockflow.orders.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderRequest(
        @NotEmpty(message = "El pedido debe contener al menos una línea")
        @Valid
        List<OrderLineRequest> lines
) {
}
