package com.example.marketplacemanagement.service;

import com.example.marketplacemanagement.entity.Product;
import com.example.marketplacemanagement.repository.ProductRepository;
import com.example.marketplacemanagement.repository.SellerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final SequenceService sequenceService;

    public ProductService( ProductRepository productRepository, SellerRepository sellerRepository, SequenceService sequenceService)
    {
        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
        this.sequenceService = sequenceService;
    }

    public Flux<Product> getAllProducts()
    {
        return productRepository.findAll();

    }

    public Mono<Product> getProductById(Long id)
    {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Product not found:" + id
                        )
                ));
    }

    public  Flux<Product> getProductsByCategory(String category){
        return  productRepository.findByCategoryIgnoreCase(category);
    }

    public Flux<Product> getProductsBySellerId(Long sellerId)
    {
        return productRepository.findBySellerId(sellerId);
    }

    // it validates the product , generates its ID, then saves it to MonoDb
    public  Mono<Product> createProduct(Product product)
    {
        return  validateProduct(product)
                .then(sequenceService.nextId("products"))
                .flatMap(id -> {
                    product.setId(id);
                    return productRepository.save(product);
                });
    }
    public Mono<Product> updateProduct(long id, Product product)
    {
        return getProductById(id)
                .flatMap(existingProuct -> validateProduct(product)
                        .then(Mono.defer(() ->
                        {
                            existingProuct.setProductName( product.getProductName());
                            existingProuct.setCategory(product.getCategory());
                            existingProuct.setPrice(product.getPrice());
                            existingProuct.setStockQuantity(product.getStockQuantity());
                            existingProuct.setAvailability(product.isAvailability());
                            existingProuct.setSellerId(product.getSellerId());

                            return  productRepository.save(existingProuct);
                        }))
                );
    }

    public  Mono<Void> deleteProduct(Long id)
    {
        return  getProductById(id)
                .flatMap(productRepository::delete);
    }

    private  Mono<Void> validateProduct(Product product){
        return  Mono.defer(() -> {
            if(product.getProductName() == null || product.getProductName().isBlank()){
                return badRequest("Product name must not be empty");
            }

            if(product.getCategory() == null || product.getCategory().isBlank())
            {
                return badRequest("Category must not be empty");
            }

            if(product.getPrice() == null || product.getPrice().signum() <= 0)
            {
                return badRequest("Price must be grater than zero");
            }

            if(product.getStockQuantity() < 0)
            {
                return badRequest("Stock quantity cannot be nagative ");
            }

            if(product.getSellerId() == null)
            {
                return badRequest("Seller ID is required");
            }

            // a product must belong to seller already stored in mongoDB.
            return  sellerRepository.existsById(product.getSellerId())
                    .flatMap(exists -> {
                        if (!exists) {
                            return badRequest("Seller does not exist");
                        }
                        return Mono.<Void> empty();
                    });
        });
    }

    private Mono<Void> badRequest(String message)
    {
      return Mono.error(new ResponseStatusException( HttpStatus.BAD_REQUEST,
              message));
    }

    public Flux<Product> getLowStockProducts(int threshold)
    {
        if (threshold < 0 )
        {
            return Flux.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Threshold cannot be negative"
            ));
        }
        return  productRepository.findByStockQuantityLessThanEqual(threshold);
    }
}
