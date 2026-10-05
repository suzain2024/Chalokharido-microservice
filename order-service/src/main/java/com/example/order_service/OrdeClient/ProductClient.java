package com.example.order_service.OrdeClient;


import com.example.order_service.OrderDto.ProductResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// "product-service" matches spring.application.name registered in Eureka!
@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/api/product/get/{id}")
    ProductResponseDto getProductById(@PathVariable("id") Long id);
}
