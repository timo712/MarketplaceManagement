package com.example.marketplacemanagement.repository;

import com.example.marketplacemanagement.entity.Seller;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface SellerRepository extends ReactiveMongoRepository<Seller, Long>{

}
