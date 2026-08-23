package com.sergio.stockflow.product.service;

import com.sergio.stockflow.common.exception.ConflictException;
import com.sergio.stockflow.product.dto.ProductRequest;
import com.sergio.stockflow.product.dto.ProductResponse;
import com.sergio.stockflow.product.entity.ProductEntity;
import com.sergio.stockflow.product.repository.ProductRepository;
import com.sergio.stockflow.common.exception.GlobalExceptionHandler;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void save(ProductRequest productRequest) {

        if(productRepository.existsBySku(productRequest.sku())) {
            throw new ConflictException("Product with SKU '" + productRequest.sku() +"' already exists");
        }

        ProductEntity productEntity1 = toEntity(productRequest);
        productRepository.save(productEntity1);
    }

    public List<ProductResponse> getAllProduct() {

        List<ProductEntity> productEntities = productRepository.findAll();
        List <ProductResponse> productResponses = new ArrayList<>();
        productEntities.forEach(productEntity -> {
            productResponses.add(toResponse(productEntity));
        });

        return productResponses;
    }

    public ProductResponse getProduct(Long id) {
        ProductEntity productEntity = productRepository.findById(id).orElseThrow(
                () -> new ConflictException("No existe el producto buscado"));
        return toResponse(productEntity);
    }

    public void editProduct(Long id, ProductRequest productRequest) {
        ProductEntity productEntity = productRepository.findById(id).orElseThrow(
                () -> new ConflictException("No existe el producto buscado"));

        if(productRepository.existsBySku(productRequest.sku())) {
            throw new ConflictException("Product with SKU '" + productEntity.getSku() +"' already exists");
        }

        ProductEntity productEntity1 = toEntity(productRequest);
        productRepository.save(productEntity1);
    }

    public void deleteProduct(Long id) {
        ProductEntity product =  productRepository.findById(id).orElseThrow(
                () -> new ConflictException("No existe el producto buscado"));

        productRepository.delete(product);
    }

    private ProductEntity toEntity(ProductRequest productRequest) {
        return new ProductEntity(
                productRequest.name(),
                productRequest.sku(),
                productRequest.price(),
                productRequest.stock()
        );
    }

    private ProductResponse toResponse(ProductEntity product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getPrice(),
                product.getStock()
        );
    }



}