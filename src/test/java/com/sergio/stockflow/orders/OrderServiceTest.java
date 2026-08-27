package com.sergio.stockflow.orders;

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
import com.sergio.stockflow.orders.service.OrdersService;
import com.sergio.stockflow.product.entity.ProductEntity;
import com.sergio.stockflow.product.repository.ProductRepository;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Conecta Mockito con el entorno de ejecución de JUnit 5.
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    OrdersRepository ordersRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    OrdersService ordersService;

    //Test 1: Calculo del total
    @Test
    void save_shouldCalculateTotalAndSaveOrder_whenProductExists() {
        // 1º creamos una petición válida.
        OrderLineRequest orderLineRequest = new OrderLineRequest(
          1L,
          20
        );

        List<OrderLineRequest> orderLineRequests = new ArrayList<>();
        orderLineRequests.add(orderLineRequest);

        OrderRequest orderRequest = new OrderRequest(
                orderLineRequests
        );

        ProductEntity product = ProductEntity.builder()
                .id(1L)
                .name("Teclado")
                .sku("TEC-001")
                .price(new BigDecimal("29.99"))
                .stock(30)
                .build();

        // 2º Simulamos que el producto existe.
        when(productRepository.findById(orderLineRequest.productId())).thenReturn(Optional.of(product));

        //3º Simulamos que el repositorio devuelve la entidad que recibe
        when(ordersRepository.save(any(OrderEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //4º Ejecutamos el metodo que estamos probando
        OrderResponse orderResponse = ordersService.save(orderRequest);

        //5º Comprobamos el total calculado
        assertEquals(
                new BigDecimal("599.80"),
                orderResponse.total()
        );

        //6º comprobamos que guardo el order
        verify(productRepository).findById(orderLineRequest.productId());
        verify(ordersRepository).save(any(OrderEntity.class));
    }

    //Test 2: Producto no existe
    @Test
    void save_shouldCalculateTotalAndSaveOrder_whenProductNotExists() {
        // 1º creamos una petición válida.
        OrderLineRequest orderLineRequest = new OrderLineRequest(
                1L,
                20
        );

        List<OrderLineRequest> orderLineRequests = new ArrayList<>();
        orderLineRequests.add(orderLineRequest);

        OrderRequest orderRequest = new OrderRequest(
                orderLineRequests
        );

        ProductEntity product = ProductEntity.builder()
                .id(1L)
                .name("Teclado")
                .sku("TEC-001")
                .price(new BigDecimal("29.99"))
                .stock(30)
                .build();

        // 2º Simulamos que el producto no existe.
        when(productRepository.findById(orderLineRequest.productId())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class, () -> ordersService.save(orderRequest)
        );

        //4º Comprobamos el mensaje
        assertEquals("Producto: " +orderLineRequest.productId() + " no encontrado",exception.getMessage());

        //6º comprobamos que guardo el order
        verify(productRepository).findById(orderLineRequest.productId());
        verify(ordersRepository,never()).save(any(OrderEntity.class));
    }

    //Test 2: Confirmar el pedido
    @Test
    void confirmTest() {

        // 1º creamos una petición válida.
        // Arrange: creamos un producto con stock suficiente.
        ProductEntity product = ProductEntity.builder()
                .id(1L)
                .name("Teclado")
                .sku("TEC-001")
                .price(new BigDecimal("29.99"))
                .stock(30)
                .build();

        // Creamos una línea que solicita 10 unidades.
        OrderLines orderLine = OrderLines.builder()
                .orderLineId(1L)
                .product(product)
                .quantity(10)
                .unitPrice(new BigDecimal("29.99"))
                .subtotal(new BigDecimal("299.90"))
                .build();

        List<OrderLines> orderLines = new ArrayList<>();
        orderLines.add(orderLine);

        // Creamos un pedido pendiente de confirmar.
        OrderEntity orderEntity = OrderEntity.builder()
                .orderId(1L)
                .status(OrderStatus.CREATED)
                .total(new BigDecimal("299.90"))
                .orderLines(orderLines)
                .build();

        // Simulamos que el pedido existe.
        when(ordersRepository.findById(1L))
                .thenReturn(Optional.of(orderEntity));

        // Act: confirmamos el pedido.
        OrderResponse response = ordersService.confirmOrder(1L);

        //Assert: comprobamos el resultado devuelto.
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(OrderStatus.CONFIRMED, response.status());
        assertEquals(new BigDecimal("299.90"), response.total());

// Comprobamos el cambio en la propia entidad.
        assertEquals(OrderStatus.CONFIRMED, orderEntity.getStatus());

// Se descontaron 10 unidades: 30 - 10 = 20.
        assertEquals(20, product.getStock());

// Comprobamos los datos de la línea devuelta.
        assertEquals(1, response.lines().size());
        assertEquals(1L, response.lines().getFirst().productId());
        assertEquals(10, response.lines().getFirst().quantity());
        assertEquals(new BigDecimal("29.99"), response.lines().getFirst().unitPrice());
        assertEquals(new BigDecimal("299.90"), response.lines().getFirst().subtotal());


        // Verify: comprobamos que se buscó el pedido.
        verify(ordersRepository).findById(1L);

        // confirmOrder no llama a save(): @Transactional guarda los cambios.
        verify(ordersRepository, never())
                .save(any(OrderEntity.class));
    }

    //Test 3: confirmar un pedido sin stock
    @Test
    void confirm_shouldThrowConflictException_whenProductHasInsufficientStock() {

        // Arrange: producto con solo 5 unidades disponibles.
        ProductEntity product = ProductEntity.builder()
                .id(1L)
                .name("Teclado")
                .sku("TEC-001")
                .price(new BigDecimal("29.99"))
                .stock(5)
                .build();

        // El pedido solicita 10 unidades.
        OrderLines orderLine = OrderLines.builder()
                .orderLineId(1L)
                .product(product)
                .quantity(10)
                .unitPrice(new BigDecimal("29.99"))
                .subtotal(new BigDecimal("299.90"))
                .build();

        OrderEntity orderEntity = OrderEntity.builder()
                .orderId(1L)
                .status(OrderStatus.CREATED)
                .total(new BigDecimal("299.90"))
                .orderLines(new ArrayList<>(List.of(orderLine)))
                .build();

        when(ordersRepository.findById(1L))
                .thenReturn(Optional.of(orderEntity));

        // Act y Assert: no se puede confirmar por falta de stock.
        assertThrows(
                ConflictException.class,
                () -> ordersService.confirmOrder(1L)
        );

        // El pedido debe continuar en estado CREATED.
        assertEquals(OrderStatus.CREATED, orderEntity.getStatus());

        // El stock no debe modificarse.
        assertEquals(5, product.getStock());

        verify(ordersRepository).findById(1L);
        verify(ordersRepository, never())
                .save(any(OrderEntity.class));
    }

    @Test
    void cancel_shouldCancelOrderAndRestoreProductStock() {

        // Arrange: el producto tiene 20 unidades después de confirmar el pedido.
        ProductEntity product = ProductEntity.builder()
                .id(1L)
                .name("Teclado")
                .sku("TEC-001")
                .price(new BigDecimal("29.99"))
                .stock(20)
                .build();

        // El pedido confirmado contenía 10 unidades.
        OrderLines orderLine = OrderLines.builder()
                .orderLineId(1L)
                .product(product)
                .quantity(10)
                .unitPrice(new BigDecimal("29.99"))
                .subtotal(new BigDecimal("299.90"))
                .build();

        OrderEntity orderEntity = OrderEntity.builder()
                .orderId(1L)
                .status(OrderStatus.CONFIRMED)
                .total(new BigDecimal("299.90"))
                .orderLines(new ArrayList<>(List.of(orderLine)))
                .build();

        when(ordersRepository.findById(1L))
                .thenReturn(Optional.of(orderEntity));

        // Act: cancelamos el pedido.
        OrderResponse response = ordersService.cancelOrder(1L);

        // Assert: comprobamos la respuesta.
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(OrderStatus.CANCELLED, response.status());
        assertEquals(new BigDecimal("299.90"), response.total());

        // La entidad debe quedar cancelada.
        assertEquals(OrderStatus.CANCELLED, orderEntity.getStatus());

        // Se recuperan las 10 unidades: 20 + 10 = 30.
        assertEquals(30, product.getStock());

        // Comprobamos la línea devuelta.
        assertEquals(1, response.lines().size());

        OrderLineResponse responseLine = response.lines().getFirst();

        assertEquals(1L, responseLine.productId());
        assertEquals(10, responseLine.quantity());
        assertEquals(new BigDecimal("29.99"), responseLine.unitPrice());
        assertEquals(new BigDecimal("299.90"), responseLine.subtotal());

        // Verify: se buscó el pedido una vez.
        verify(ordersRepository).findById(1L);

        // @Transactional detectará los cambios sobre una entidad administrada.
        verify(ordersRepository, never())
                .save(any(OrderEntity.class));
    }
}
