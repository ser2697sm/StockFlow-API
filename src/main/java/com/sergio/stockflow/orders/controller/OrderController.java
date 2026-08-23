package com.sergio.stockflow.orders.controller;

import com.sergio.stockflow.orders.dto.OrderRequest;
import com.sergio.stockflow.orders.dto.OrderResponse;
import com.sergio.stockflow.orders.service.OrdersService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrdersService ordersService;

    public OrderController(OrdersService ordersService) {
        this.ordersService = ordersService;
    }

    //Registrar una compra o pedido compuesto por uno o varios productos.
    @PostMapping
    public ResponseEntity<OrderResponse> saveOrders(@Valid @RequestBody OrderRequest orderRequest) {
        return ResponseEntity.ok(ordersService.save(orderRequest));
    }

    @PostMapping("{id}/confirm")
    public ResponseEntity<?> confirmOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ordersService.confirmOrder(id));
    }

    @PostMapping("{id}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ordersService.cancelOrder(id));
    }

    //Devuelve el listado de todas las compras
    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        return ResponseEntity.ok(ordersService.getALlOrders());
    }

    //Devuelve el listado de una compra en concreto
    @GetMapping("{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ordersService.getOrder(id));
    }
}
