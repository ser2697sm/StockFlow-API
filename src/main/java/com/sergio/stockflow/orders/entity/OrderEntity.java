package com.sergio.stockflow.orders.entity;

import com.sergio.stockflow.orders.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    //Estado del pedido
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    //importe total calculado por el backend
    @Column(nullable = false)
    private BigDecimal total;

    //Líneas del pedido
    @OneToMany(mappedBy = "order",cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderLines> orderLines = new ArrayList<>();

    //Fecha y hora de creación
    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    //Fecha y hora de la última modificación
    @Column(name = "updated_at",nullable = false)
    private LocalDateTime updatedAt;

    //@PrePersist: se ejecuta automáticamente justo antes del primer INSERT
    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    //@PreUpdate: se ejecuta antes de un UPDATE
    @PreUpdate
    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addLine(OrderLines line) {
        orderLines.add(line);
    }

    public void updateTotal(BigDecimal total) {
        this.total = total;
    }
}
