package com.sergio.stockflow.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class ProductEntity {

    public ProductEntity(
            String name,
            String sku,
            BigDecimal price,
            Integer stock
    ) {
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.stock = stock;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false,unique = true,length = 50)
    private String sku;

    // precision = permite un máximo de 12 dígitos en total.
    //scale = dos de esos dígitos pertenecen a la parte decimal
    @Column(nullable = false,precision = 12,scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "created_at",nullable = false,updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at",nullable = false)
    private OffsetDateTime updatedAt;

    // Inicializa las fechas antes de insertar el producto.
    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    // Actualiza la fecha de modificación antes de guardar cambios.
    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
