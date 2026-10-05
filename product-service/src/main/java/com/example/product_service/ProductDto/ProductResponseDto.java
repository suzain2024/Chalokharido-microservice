package com.example.product_service.ProductDto;

import java.io.Serializable;

public class ProductResponseDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private double price;
    private String id;
    private Integer stock;

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public double getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public Integer getStock() {
        return stock;
    }
}