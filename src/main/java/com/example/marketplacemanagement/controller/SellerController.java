package com.example.marketplacemanagement.controller;

import com.example.marketplacemanagement.entity.Seller;
import com.example.marketplacemanagement.service.SellerService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/sellers")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService){
        this.sellerService = sellerService;
    }

    @GetMapping
    public Flux<Seller> getAllSellers(){
        return sellerService.getAllSellers();
    }

    @GetMapping("/{id}")
    public Mono<Seller> getSellerById(@PathVariable Long id){
        return sellerService.getSellerById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Seller> createSeller(@RequestBody Seller seller){
        return  sellerService.createSeller(seller);
    }

    @PutMapping("/{id}")
    public Mono<Seller> updateSeller(
            @PathVariable Long id,
            @RequestBody Seller seller){
        return sellerService.updateSeller(id, seller);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteSeller(@PathVariable long id){
        return sellerService.deleteSeller(id);
    }
}
