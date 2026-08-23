package com.sergio.stockflow.orders.entity;

import com.sergio.stockflow.product.entity.ProductEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "orderLines")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderLines {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_line_id")
    private Long orderLineId;

    // Pedido al que pertenece la línea.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    // Producto incluido en la línea.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    //Cantidad solicitada
    @Column(nullable = false)
    private Integer quantity;

    // Precio del producto en el momento de crear el pedido.
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    // unitPrice multiplicado por quantity.
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;



}
