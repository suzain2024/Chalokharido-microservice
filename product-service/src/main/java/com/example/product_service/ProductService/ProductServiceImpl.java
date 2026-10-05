package com.example.product_service.ProductService;

import com.example.product_service.ProductDto.ProductRequestDto;
import com.example.product_service.ProductDto.ProductResponseDto;
import com.example.product_service.ProductEntity.ProductEntity;
import com.example.product_service.ProductRepository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductResponseDto createProduct(ProductRequestDto product) {
        ProductEntity newProduct = new ProductEntity();
        newProduct.setName(product.getName()); 
        newProduct.setPrice(product.getPrice());
        ProductEntity createdProduct = productRepository.save(newProduct);

        ProductResponseDto p = new ProductResponseDto();
        p.setId(String.valueOf(createdProduct.getId()));
        p.setName(createdProduct.getName());
        p.setPrice(createdProduct.getPrice());
        p.setStock(createdProduct.getStock());
        return p;
    }

    @Cacheable(value = "products", key = "#id")
    @Override
    public ProductResponseDto getProductById(Long id) {
        log.info("[REDIS CACHE MISS] Querying PostgreSQL for product ID: {}", id);
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found " + id));

        ProductResponseDto newProduct = new ProductResponseDto();
        newProduct.setId(String.valueOf(product.getId()));
        newProduct.setName(product.getName());
        newProduct.setPrice(product.getPrice());
        newProduct.setStock(product.getStock());

        return newProduct;
    }

    @Override
    public List<ProductResponseDto> getAll() {
        List<ProductResponseDto> productList = new ArrayList<>();
        List<ProductEntity> product = productRepository.findAll();

        for (ProductEntity pro : product) {
            ProductResponseDto newProduct = new ProductResponseDto();
            newProduct.setId(String.valueOf(pro.getId()));
            newProduct.setName(pro.getName());
            newProduct.setPrice(pro.getPrice());
            newProduct.setStock(pro.getStock());
            productList.add(newProduct);
        }
        return productList;
    }

    @CacheEvict(value = "products", key = "#id")
    @Override
    public ProductResponseDto updateById(ProductRequestDto pro, Long id) {
        log.info("[REDIS CACHE EVICT] Updating database and clearing Redis cache for product ID: {}", id);
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found " + id));
        product.setName(pro.getName());
        product.setPrice(pro.getPrice());
        ProductEntity p = productRepository.save(product);

        ProductResponseDto newProduct = new ProductResponseDto();
        newProduct.setId(String.valueOf(p.getId()));
        newProduct.setName(p.getName());
        newProduct.setPrice(p.getPrice());
        newProduct.setStock(p.getStock());

        return newProduct;
    }

    @CacheEvict(value = "products", key = "#id")
    @Override
    public ProductResponseDto deleteById(Long id) {
        log.info("[REDIS CACHE EVICT] Deleting from database and clearing Redis cache for product ID: {}", id);
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found " + id));
        productRepository.delete(product);

        ProductResponseDto newProduct = new ProductResponseDto();
        newProduct.setId(String.valueOf(product.getId()));
        newProduct.setName(product.getName());
        newProduct.setPrice(product.getPrice());
        newProduct.setStock(product.getStock());
        return newProduct;
    }
}