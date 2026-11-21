package com.example.marketplacemanagement.repository;

import com.example.marketplacemanagement.entity.CustomerOrder;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface CustomerOrderRepository extends
        ReactiveMongoRepository<CustomerOrder, Long > {

    Flux<CustomerOrder> findByOrderStatusIgnoreCase(String orderStatus);

    Flux<CustomerOrder> findByProductId(Long productId);
}
