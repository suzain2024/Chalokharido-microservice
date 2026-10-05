package com.example.product_service.ProductDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProductRequestDto {
    @NotBlank(message = "name can't be empty")
    String name;

    @NotNull(message = "price can't be null")
    Double price;

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }
}
