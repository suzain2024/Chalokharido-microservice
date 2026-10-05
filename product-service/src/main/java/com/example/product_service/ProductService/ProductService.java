package com.example.product_service.ProductService;

import com.example.product_service.ProductDto.ProductRequestDto;
import com.example.product_service.ProductDto.ProductResponseDto;

import java.util.List;

public interface ProductService {
    ProductResponseDto createProduct(ProductRequestDto product);
    ProductResponseDto getProductById(Long id);
    List<ProductResponseDto> getAll();
    ProductResponseDto updateById(ProductRequestDto pro, Long id);
    ProductResponseDto deleteById(Long id);
}
