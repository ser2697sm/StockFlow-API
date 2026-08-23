package com.sergio.stockflow.orders.repository;

import com.sergio.stockflow.orders.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<OrderEntity,Long> {
}
