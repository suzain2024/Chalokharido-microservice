package com.example.product_service.ProductController;

import com.example.product_service.ProductDto.ProductRequestDto;
import com.example.product_service.ProductDto.ProductResponseDto;
import com.example.product_service.ProductService.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create")
    public ResponseEntity<ProductResponseDto> createProduct(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @Valid @RequestBody ProductRequestDto product) {
        ProductResponseDto newProduct = productService.createProduct(product);
        return new ResponseEntity<>(newProduct, HttpStatus.CREATED);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        ProductResponseDto product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/get")
    public ResponseEntity<List<ProductResponseDto>> getAllProduct() {
        List<ProductResponseDto> productList = productService.getAll();
        return ResponseEntity.ok(productList);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ProductResponseDto> updateProductById(@Valid @RequestBody ProductRequestDto pro, @PathVariable Long id) {
        ProductResponseDto product = productService.updateById(pro, id);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/delete/{id}")
    public ProductResponseDto deleteProductById(@PathVariable Long id) {
        ProductResponseDto deletedProduct = productService.deleteById(id);
        return deletedProduct;
    }
}