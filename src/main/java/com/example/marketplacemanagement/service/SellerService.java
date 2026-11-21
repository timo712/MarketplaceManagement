package com.example.marketplacemanagement.service;


import com.example.marketplacemanagement.entity.Seller;
import com.example.marketplacemanagement.repository.SellerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SellerService {

    private final SellerRepository sellerRepository;

    private final SequenceService sequenceService;

    public SellerService(SellerRepository sellerRepository,
                         SequenceService sequenceService){
        this.sellerRepository = sellerRepository;
        this.sequenceService = sequenceService;
    }

    public Flux<Seller> getAllSellers(){
        return  sellerRepository.findAll();
    }

    public Mono<Seller> getSellerById(Long id){
        return sellerRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Seller not found: " + id
                        )
                ));
    }

    public Mono<Seller> createSeller(Seller seller) {
        return  Mono.defer(() -> {
            validateSeller(seller);

            // Always generate the ID instead of accepting a supplied Id
            return sequenceService.nextId("sellers")
                    .flatMap(id -> {
                        seller.setId(id);
                        return sellerRepository.save(seller);
                    });
        });
    }

    public Mono<Seller> updateSeller(Long id, Seller seller) {
        return getSellerById(id)
                .flatMap(existingSeller -> {
                    validateSeller(seller);

                    existingSeller.setSellerName(seller.getSellerName());
                    existingSeller.setEmail(seller.getEmail());
                    existingSeller.setBusinessType(seller.getBusinessType());
                    existingSeller.setCity(seller.getCity());
                    existingSeller.setRating(seller.getRating());

                    return  sellerRepository.save(existingSeller);
                });
    }

    public  Mono<Void> deleteSeller(Long id){
        return getSellerById(id)
                .flatMap(sellerRepository::delete);
    }

    private void validateSeller(Seller seller){
        if(seller.getSellerName() == null
                || seller.getSellerName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Seller name must not be empty"
            );
        }
        if (seller.getEmail() == null
                || seller.getEmail().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Seller email must not be empty"
            );
        }
    }
}
