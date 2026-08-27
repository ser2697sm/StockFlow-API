package com.sergio.stockflow.orders.service;

import com.sergio.stockflow.common.exception.ConflictException;
import com.sergio.stockflow.common.exception.ResourceNotFoundException;
import com.sergio.stockflow.orders.dto.OrderLineRequest;
import com.sergio.stockflow.orders.dto.OrderLineResponse;
import com.sergio.stockflow.orders.dto.OrderRequest;
import com.sergio.stockflow.orders.dto.OrderResponse;
import com.sergio.stockflow.orders.entity.OrderEntity;
import com.sergio.stockflow.orders.entity.OrderLines;
import com.sergio.stockflow.orders.enums.OrderStatus;
import com.sergio.stockflow.orders.repository.OrdersRepository;
import com.sergio.stockflow.product.entity.ProductEntity;
import com.sergio.stockflow.product.repository.ProductRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OrdersService {

    private final OrdersRepository ordersRepository;
    private final ProductRepository productRepository;

    public OrdersService(OrdersRepository ordersRepository, ProductRepository productRepository) {
        this.ordersRepository = ordersRepository;
        this.productRepository = productRepository;
    }

    public OrderResponse save(OrderRequest orderRequest) {

        // Creamos el pedido con su estado inicial y el total a cero.
        OrderEntity order = OrderEntity.builder()
                .status(OrderStatus.CREATED)
                .total(BigDecimal.ZERO)
                .build();

        // Acumulador donde sumaremos el importe de todas las líneas
        BigDecimal total = BigDecimal.ZERO;

        // Recorremos los productos y cantidades recibidos en la peticion
        for (OrderLineRequest lineRequest : orderRequest.lines()) {
            // Buscamos el producto para comprobar que existe y obtener su precio real
            ProductEntity product =  productRepository.findById(lineRequest.productId()).orElseThrow(() ->
                    new ResourceNotFoundException("Producto: " + lineRequest.productId() + " no encontrado"));

            // Calculamos el subtotal: precio actual del producto * cantidad.
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(lineRequest.quantity()));

            // Creamos la línea guardando el producto, la cantidad y su precio actual
            OrderLines line = OrderLines.builder()
                    .order(order)
                    .product(product)
                    .quantity(lineRequest.quantity())
                    .unitPrice(product.getPrice())
                    .subtotal(subtotal)
                    .build();

            // Añadimos la línea al pedido y acumulamos su subtotal
            order.addLine(line);
            total = total.add(subtotal);
        }
        // Asignamos al pedido el importe total calculado.
        order.setTotal(total);

        // Guardamos el pedido y, gracias al cascade, también todas sus líneas.
        OrderEntity savedOrder = ordersRepository.save(order);

        // Convertimos la entidad guardada en el DTO que devolverá la API.
        return toResponse(savedOrder);
    }

    // @Transactional(readOnly = true) -> Transacción solo de lectura para consultar el pedido y sus relaciones LAZY.
    @Transactional(readOnly = true)
    public List<OrderResponse> getALlOrders() {
        return  ordersRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse confirmOrder(Long id) {

        //Buscamos el pedido
        OrderEntity order = ordersRepository.findById(id)
                .orElseThrow(() -> new ConflictException("Order: " + id + " no encontrado"));

        // Comprobamos antes de modificar nada que no esté confirmado.
        if(order.getStatus() == OrderStatus.CONFIRMED) {
            throw new ConflictException(
                    "El pedido: " + id + " ya está confirmado"
            );
        }

        // Comprobamos y descontamos el stock de cada línea.
        order.getOrderLines().forEach(orderLine -> {
            ProductEntity product = orderLine.getProduct();
            int requestedQuantity = orderLine.getQuantity();

            if (product.getStock() < requestedQuantity) {
                throw new ConflictException("Stock insuficiente para el producto: " + product.getId());
            }

            product.setStock(product.getStock() - requestedQuantity);
        });

        // Confirmamos el pedido solamente cuando todas las líneas son válidas.
        order.setStatus(OrderStatus.CONFIRMED);
        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {

        //Buscamos el pedido
        OrderEntity order = ordersRepository.findById(id)
                .orElseThrow(() -> new ConflictException("Order: " + id + " no encontrado"));

        // Evitamos cancelar dos veces y recuperar el stock nuevamente.
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ConflictException(
                    "El pedido: " + id + " ya está cancelado"
            );
        }

        // Solo se pueden cancelar pedidos confirmados.
        if(order.getStatus() != OrderStatus.CONFIRMED) {
            throw new ConflictException("El pedido: " + id + "  no se puede cancelar porque no está confirmado");
        }

        // Recuperamos el stock de cada línea.
        order.getOrderLines().forEach(orderLine -> {
            ProductEntity product = orderLine.getProduct();
            int requestedQuantity = orderLine.getQuantity();

            product.setStock(product.getStock() + requestedQuantity);
        });

        // Mrcamos el pedido como cancelado
        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(order);
    }

    // @Transactional(readOnly = true) -> Transacción solo de lectura para consultar el pedido y sus relaciones LAZY.
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        OrderEntity order = ordersRepository.findById(id)
                .orElseThrow(() -> new ConflictException("Order: " + id + " no encontrado"));

        return toResponse(order);
    }

    private OrderResponse toResponse(OrderEntity order) {

        List<OrderLineResponse> lines = order.getOrderLines()
                .stream()
                .map(line -> new OrderLineResponse(
                        line.getProduct().getId(),
                        line.getProduct().getName(),
                        line.getQuantity(),
                        line.getUnitPrice(),
                        line.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getOrderId(),
                order.getStatus(),
                order.getTotal(),
                lines,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }


}
