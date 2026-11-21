package com.example.marketplacemanagement.service;


import com.example.marketplacemanagement.entity.CustomerOrder;
import com.example.marketplacemanagement.repository.CustomerOrderRepository;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import com.example.marketplacemanagement.entity.Product;
import java.time.Instant;
import java.util.Set;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import java.util.Objects;

@Service
public class CustomerOrderService {
    private  final CustomerOrderRepository orderRepository;
    private  final ProductService productService;
    private  final SequenceService sequenceService;
    private  final ReactiveMongoTemplate mongoTemplate;



    public CustomerOrderService(CustomerOrderRepository orderRepository,
                                 ProductService productService,
                                SequenceService sequenceService,
                                ReactiveMongoTemplate mongoTemplate)
    {
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.sequenceService =  sequenceService;
        this.mongoTemplate = mongoTemplate;
    }

    public Flux<CustomerOrder> getAllOrders()
    {
        return  orderRepository.findAll();
    }

    public Mono<CustomerOrder> getOrderById(Long id)
    {
        return  orderRepository.findById(id)
                // returns 404 when the order doesn't exist
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found:" + id)
                ));
    }

    public Flux<CustomerOrder> getOrdersByStatus (String status)
    {
        return orderRepository.findByOrderStatusIgnoreCase(status);
    }

    public Flux<CustomerOrder> getOrdersByProductId(Long productId)
    {
        return  orderRepository.findByProductId(productId);
    }
    public Mono<Void> deleteOrder(Long id)
    {
        return getOrderById(id)
                .flatMap(orderRepository::delete);
    }

    public Mono<CustomerOrder> createOrder(CustomerOrder order){
        return Mono.defer(() -> {
            validateOrder(order);

            //confirm the product exists before generating the order ID
            return  productService.getProductById(order.getProductId())
                    .then(sequenceService.nextId("order"))
                    .flatMap(id -> {
                        order.setId(id);
                        order.setOrderTime(Instant.now());

                        return  reserveStockAndSave(order);
                    });
        });
    }

    private  void  validateOrder(CustomerOrder order)
    {
        if(order.getCustomerName() == null || order.getCustomerName().isBlank())
        {
            throw  new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Customer name is required"
            );
        }

        if(order.getCustomerEmail() == null || order.getCustomerEmail().isBlank())
        {
            throw  new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Customer email is required"
            );
        }

        if (order.getProductId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product ID is required"
            );
        }

        if (order.getQuantity() <= 0 )
        {
            throw  new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero"
            );
        }

        if(order.getOrderStatus() == null)
        {
            order.setOrderStatus("Pending");
        }

        Set<String> allowedStatuses = Set.of(
                "Pending", "Confirmed", "Shipped","Delivered"
        );

        if(!allowedStatuses.contains(order.getOrderStatus()))
        {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be pending, Confirmed, Shipped, or Delivered"
            );

        }
    }

    private  Mono<CustomerOrder> reserveStockAndSave(CustomerOrder order)
    {
        Query stockQuery = Query.query(
                Criteria.where("_id").is(order.getProductId())
                        .and("availability").is(true)
                        .and("stockQuantity").gte(order.getQuantity())
        );

        Update reduceStock = new Update()
                .inc("stockQuantity", -order.getQuantity());

        //check stock and reduce it in one atomic MongoDb operation
        return  mongoTemplate.findAndModify(
                stockQuery,
                reduceStock,
                FindAndModifyOptions.options().returnNew(true),
                Product.class
        )
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Product is unavailable or insufficient stock"
                        )
                ))
                .flatMap(product -> orderRepository.save(order)
                        .onErrorResume(error ->
                                restoreStock(order)
                                .then(Mono.<CustomerOrder>error(error))
                        )
                );
    }

    private Mono<Void> restoreStock(CustomerOrder order)
    {
        Query query = Query.query(
                Criteria.where("_id").is(order.getProductId())
        );

        Update restore = new Update()
                .inc("stockQuantity", order.getQuantity());

        return mongoTemplate.updateFirst(query,restore, Product.class)
                .then();
    }


    public  Mono<CustomerOrder> updateOrder(Long id, CustomerOrder order)
    {
        return  getOrderById(id)
                .flatMap(existingOrder -> {
                    validateOrder(order);

                    if(!Objects.equals(
                            existingOrder.getProductId(),
                            order.getProductId()
                    )){
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "The product cannot be changed after order creation "
                        ));
                    }

                    if (existingOrder.getQuantity() != order.getQuantity())
                    {
                        return  Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "The quantity cannot be changed after order creation"
                        ));
                    }

                    existingOrder.setCustomerName(order.getCustomerName());
                    existingOrder.setCustomerEmail(order.getCustomerEmail());
                    existingOrder.setOrderStatus(order.getOrderStatus());

                    // Keeps the original ID, product, quantity, and order time
                    return  orderRepository.save(existingOrder);
                });
    }
}
