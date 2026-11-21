package com.example.marketplacemanagement.repository;

import com.example.marketplacemanagement.entity.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ProductRepository extends ReactiveMongoRepository<Product, Long> {

    Flux<Product> findByCategoryIgnoreCase(String category);

    Flux<Product> findBySellerId(Long sellerId);

    Flux<Product> findByStockQuantityLessThanEqual(int threshold);
}
