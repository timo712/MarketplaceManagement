package com.example.marketplacemanagement.controller;


import com.example.marketplacemanagement.entity.CustomerOrder;
import com.example.marketplacemanagement.service.CustomerOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/orders")
public class CustomerOrderController {
    private final CustomerOrderService orderService;

    public CustomerOrderController(CustomerOrderService orderService)
    {
        this.orderService = orderService;

    }

    @GetMapping
    public Flux<CustomerOrder> getAllOrders(){
        return orderService.getAllOrders();
    }

    @GetMapping("/{id}")
    public Mono<CustomerOrder> getOrderById(@PathVariable Long id)
    {
        return orderService.getOrderById(id);
    }

    @GetMapping("/status/{status}")
    public Flux<CustomerOrder> getOrdersByStatus(
            @PathVariable String status)
    {
        return orderService.getOrdersByStatus(status);
    }

    @GetMapping("/product/{productId}")
    public Flux<CustomerOrder> getOrdersByProductId(
            @PathVariable Long productId)
    {
        return orderService.getOrdersByProductId(productId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CustomerOrder> createOrder(
            @RequestBody CustomerOrder order)
    {
        return orderService.createOrder(order);
    }

    @PutMapping("/{id}")
    public Mono<CustomerOrder> updateOrder(
            @PathVariable Long id,
            @RequestBody CustomerOrder order)
    {
        return orderService.updateOrder(id, order);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteOrder(@PathVariable Long id)
    {
        return orderService.deleteOrder(id);
    }
}
