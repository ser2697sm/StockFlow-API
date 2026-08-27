package com.sergio.stockflow.product;

import com.sergio.stockflow.common.exception.ConflictException;
import com.sergio.stockflow.product.dto.ProductRequest;
import com.sergio.stockflow.product.entity.ProductEntity;
import com.sergio.stockflow.product.repository.ProductRepository;
import com.sergio.stockflow.product.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Conecta Mockito con el entorno de ejecución de JUnit 5.
@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    //(@Mock, @InjectMocks): Crea simulaciones de objetos y los inyecta en la clase que deseas poner a prueba.

    @Mock
    ProductRepository productRepository;

    @InjectMocks
    ProductService productService;

    //Test 1: guarda el producto cuando el SKU no existe
    @Test
    void save_shouldSaveProduct_whenSkuDoesNotExist() {
        // 1º creamos una petición válida.
        ProductRequest productRequest = new ProductRequest(
                "Teclado",
                "TEC-001",
                new BigDecimal("29.99"),
                10
        );

        //2º Indicamos que no existe el Sku
        when(productRepository.existsBySku("TEC-001")).thenReturn(false);

        //3º Ejecutar el metodo probado
        productService.save(productRequest);

        //4º comprobamos que consultó el SKU y guardó el producto.
        verify(productRepository).existsBySku("TEC-001");
        verify(productRepository).save(any(ProductEntity.class));

    }

    //Test 2: no guarda el producto por que el SKU existe
    @Test
    void save_shouldThrowConflictException_whenSkuAlreadyExists() {
        // 1º creamos una petición válida.
        ProductRequest productRequest = new ProductRequest(
                "Teclado",
                "TEC-001",
                new BigDecimal("29.99"),
                10
        );

        //2º Indicamos que existe el Sku
        when(productRepository.existsBySku("TEC-001")).thenReturn(true);

        //3º Ejecutar el metodo probado, pero en este caso como va a existir un SKU se lanza un ConflictException
        // que es lo esperado
        ConflictException exception = assertThrows(
                ConflictException.class, () -> productService.save(productRequest)
        );

        //4º Comprobamos el mensaje
        assertEquals("Product with SKU 'TEC-001' already exists",exception.getMessage());

        //4º comprobamos que consultó el SKU, pero no guardo el producto
        verify(productRepository).existsBySku("TEC-001");
        verify(productRepository,never()).save(any(ProductEntity.class));

    }

}
