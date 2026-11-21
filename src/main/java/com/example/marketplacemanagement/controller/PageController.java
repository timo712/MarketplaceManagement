package com.example.marketplacemanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home()
    {
        return "index";
    }

    @GetMapping("/sellers")
    public String sellers() {
        return "sellers";
    }

    @GetMapping("/products")
    public String products() {
        return "products";
    }

    @GetMapping("/orders")
    public String orders() {
        return "orders";
    }


}
