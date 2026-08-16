package com.sergio.stockflow.product.repository;

import com.sergio.stockflow.product.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity,Long> {

    boolean existsBySku(String sku);

}
